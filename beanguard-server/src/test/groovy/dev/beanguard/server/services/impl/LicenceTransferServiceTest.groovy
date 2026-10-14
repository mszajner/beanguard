package dev.beanguard.server.services.impl

import dev.beanguard.server.entities.LicenceEntity
import dev.beanguard.server.entities.LicenceTransferEntity
import dev.beanguard.server.exceptions.LicenceTransferExpiredException
import dev.beanguard.server.exceptions.LicenceTransferNotFoundException
import dev.beanguard.server.ports.InstantProvider
import dev.beanguard.server.ports.SecureStringProvider
import dev.beanguard.server.repositories.LicenceRepository
import dev.beanguard.server.repositories.LicenceTransferRepository
import dev.beanguard.server.models.ParameterName
import dev.beanguard.server.services.MailService
import dev.beanguard.server.services.ParameterService
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

class LicenceTransferServiceTest extends Specification {

    def licenceTransferRepository = Mock(LicenceTransferRepository)
    def licenceRepository = Mock(LicenceRepository)
    def mailService = Mock(MailService)
    def secureStringProvider = Mock(SecureStringProvider)
    def instantProvider = Mock(InstantProvider)
    def parameterService = Mock(ParameterService)
    def service = new LicenceTransferServiceImpl(
            licenceTransferRepository, licenceRepository, mailService,
            secureStringProvider, instantProvider, parameterService)

    def now = Instant.now()

    def setup() {
        ReflectionTestUtils.setField(service, "shopBaseUrl", "http://localhost:8001")
        instantProvider.now() >> now
        parameterService.getString(ParameterName.TRANSFER_INIT_COOLDOWN_SECONDS) >> "60"
    }

    private LicenceEntity licence(UUID key, String email, String secret = "old-secret") {
        def licence = new LicenceEntity()
        licence.key = key
        licence.email = email
        licence.secret = secret
        licence
    }

    private LicenceTransferEntity transfer(UUID token, UUID licenceKey, UUID confirmToken, String status,
                                           Instant expiresAt = now.plus(1, ChronoUnit.HOURS)) {
        new LicenceTransferEntity(token, licenceKey, confirmToken, status, expiresAt, now.minus(1, ChronoUnit.MINUTES))
    }

    def "initiateTransfer saves a transfer, emails only the confirmation token and returns the poll token"() {
        given:
        def key = UUID.randomUUID()
        licenceRepository.findByKey(key) >> Optional.of(licence(key, "test@example.com"))
        licenceTransferRepository.existsByLicenceKeyAndStatusAndCreatedAtAfter(key, "PENDING", _) >> false
        LicenceTransferEntity saved = null

        when:
        def result = service.initiateTransfer(key)

        then:
        1 * licenceTransferRepository.deleteByLicenceKeyAndStatus(key, "PENDING")
        1 * licenceTransferRepository.save(_) >> { LicenceTransferEntity e -> saved = e; e }
        1 * mailService.sendTransferConfirmation("test@example.com", _) >> { to, url ->
            assert url == "http://localhost:8001/transfer-confirm?token=${saved.confirmToken}"
            assert !url.contains(saved.token.toString())
        }
        result.transferToken() == saved.token
        saved.token != saved.confirmToken
        result.expiresAt() != null
    }

    def "initiateTransfer for an unknown key returns a token but stores and sends nothing"() {
        given:
        def key = UUID.randomUUID()
        licenceRepository.findByKey(key) >> Optional.empty()

        when:
        def result = service.initiateTransfer(key)

        then:
        result.transferToken() != null
        result.expiresAt() != null
        0 * licenceTransferRepository.save(_)
        0 * mailService._
    }

    def "initiateTransfer for a licence without email returns a token but stores and sends nothing"() {
        given:
        def key = UUID.randomUUID()
        licenceRepository.findByKey(key) >> Optional.of(licence(key, " "))

        when:
        def result = service.initiateTransfer(key)

        then:
        result.transferToken() != null
        0 * licenceTransferRepository.save(_)
        0 * mailService._
    }

    def "initiateTransfer within the cooldown keeps the pending transfer and sends nothing"() {
        given:
        def key = UUID.randomUUID()
        licenceRepository.findByKey(key) >> Optional.of(licence(key, "test@example.com"))
        licenceTransferRepository.existsByLicenceKeyAndStatusAndCreatedAtAfter(key, "PENDING", now.minusSeconds(60)) >> true

        when:
        def result = service.initiateTransfer(key)

        then:
        result.transferToken() != null
        0 * licenceTransferRepository.deleteByLicenceKeyAndStatus(_, _)
        0 * licenceTransferRepository.save(_)
        0 * mailService._
    }

    def "getStatus throws LicenceTransferNotFoundException for unknown token"() {
        given:
        licenceTransferRepository.findByTokenForUpdate(_) >> Optional.empty()

        when:
        service.getStatus(UUID.randomUUID())

        then:
        thrown(LicenceTransferNotFoundException)
    }

    def "getStatus returns EXPIRED and deletes the transfer when TTL passed"() {
        given:
        def token = UUID.randomUUID()
        def entity = transfer(token, UUID.randomUUID(), UUID.randomUUID(), "PENDING", now.minus(1, ChronoUnit.MINUTES))
        licenceTransferRepository.findByTokenForUpdate(token) >> Optional.of(entity)

        when:
        def result = service.getStatus(token)

        then:
        result.status() == "EXPIRED"
        result.secret() == null
        1 * licenceTransferRepository.delete(entity)
    }

    def "getStatus returns PENDING without a secret while unconfirmed"() {
        given:
        def token = UUID.randomUUID()
        licenceTransferRepository.findByTokenForUpdate(token) >> Optional.of(
                transfer(token, UUID.randomUUID(), UUID.randomUUID(), "PENDING"))

        when:
        def result = service.getStatus(token)

        then:
        result.status() == "PENDING"
        result.secret() == null
        0 * licenceRepository.save(_)
    }

    def "getStatus on a confirmed transfer rotates the secret, returns it and consumes the transfer"() {
        given:
        def token = UUID.randomUUID()
        def key = UUID.randomUUID()
        def entity = transfer(token, key, UUID.randomUUID(), "CONFIRMED")
        def licence = licence(key, "test@example.com")
        licenceTransferRepository.findByTokenForUpdate(token) >> Optional.of(entity)
        licenceRepository.findByKey(key) >> Optional.of(licence)
        secureStringProvider.generate(80) >> "new-secret-80chars"

        when:
        def result = service.getStatus(token)

        then:
        result.status() == "CONFIRMED"
        result.secret() == "new-secret-80chars"
        licence.secret == "new-secret-80chars"
        1 * licenceRepository.save(licence)
        1 * licenceTransferRepository.delete(entity)
    }

    def "confirm throws LicenceTransferNotFoundException for unknown token"() {
        given:
        licenceTransferRepository.findByConfirmTokenForUpdate(_) >> Optional.empty()

        when:
        service.confirm(UUID.randomUUID())

        then:
        thrown(LicenceTransferNotFoundException)
    }

    def "confirm throws LicenceTransferExpiredException for expired token"() {
        given:
        def confirmToken = UUID.randomUUID()
        licenceTransferRepository.findByConfirmTokenForUpdate(confirmToken) >> Optional.of(
                transfer(UUID.randomUUID(), UUID.randomUUID(), confirmToken, "PENDING", now.minus(1, ChronoUnit.MINUTES)))

        when:
        service.confirm(confirmToken)

        then:
        thrown(LicenceTransferExpiredException)
    }

    def "confirm throws LicenceTransferExpiredException for already-confirmed token"() {
        given:
        def confirmToken = UUID.randomUUID()
        licenceTransferRepository.findByConfirmTokenForUpdate(confirmToken) >> Optional.of(
                transfer(UUID.randomUUID(), UUID.randomUUID(), confirmToken, "CONFIRMED"))

        when:
        service.confirm(confirmToken)

        then:
        thrown(LicenceTransferExpiredException)
    }

    def "confirm marks the transfer CONFIRMED without touching the licence secret"() {
        given:
        def confirmToken = UUID.randomUUID()
        def entity = transfer(UUID.randomUUID(), UUID.randomUUID(), confirmToken, "PENDING")
        licenceTransferRepository.findByConfirmTokenForUpdate(confirmToken) >> Optional.of(entity)

        when:
        service.confirm(confirmToken)

        then:
        entity.status == "CONFIRMED"
        1 * licenceTransferRepository.save(entity)
        0 * licenceRepository.save(_)
        0 * secureStringProvider.generate(_)
    }
}
