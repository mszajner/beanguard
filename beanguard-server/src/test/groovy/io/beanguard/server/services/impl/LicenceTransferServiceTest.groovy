package io.beanguard.server.services.impl

import io.beanguard.server.entities.LicenceEntity
import io.beanguard.server.entities.LicenceTransferEntity
import io.beanguard.server.exceptions.LicenceNotFoundException
import io.beanguard.server.exceptions.LicenceTransferExpiredException
import io.beanguard.server.exceptions.LicenceTransferInitException
import io.beanguard.server.exceptions.LicenceTransferNotFoundException
import io.beanguard.server.ports.InstantProvider
import io.beanguard.server.ports.SecureStringProvider
import io.beanguard.server.repositories.LicenceRepository
import io.beanguard.server.repositories.LicenceTransferRepository
import io.beanguard.server.services.MailService
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
    def service = new LicenceTransferServiceImpl(
            licenceTransferRepository, licenceRepository, mailService,
            secureStringProvider, instantProvider)

    def setup() {
        ReflectionTestUtils.setField(service, "shopBaseUrl", "http://localhost:8001")
    }

    def "initiateTransfer throws LicenceNotFoundException for unknown key"() {
        given:
        def key = UUID.randomUUID()
        licenceRepository.findByKey(key) >> Optional.empty()

        when:
        service.initiateTransfer(key)

        then:
        thrown(LicenceNotFoundException)
    }

    def "initiateTransfer throws LicenceTransferInitException when licence has no email"() {
        given:
        def key = UUID.randomUUID()
        def licence = new LicenceEntity()
        licence.key = key
        licence.email = null
        licenceRepository.findByKey(key) >> Optional.of(licence)

        when:
        service.initiateTransfer(key)

        then:
        thrown(LicenceTransferInitException)
    }

    def "initiateTransfer deletes existing transfer, saves new one, sends email"() {
        given:
        def key = UUID.randomUUID()
        def licence = new LicenceEntity()
        licence.key = key
        licence.email = "test@example.com"
        licenceRepository.findByKey(key) >> Optional.of(licence)
        instantProvider.now() >> Instant.now()
        licenceTransferRepository.save(_) >> { LicenceTransferEntity e -> e }

        when:
        def result = service.initiateTransfer(key)

        then:
        1 * licenceTransferRepository.deleteByLicenceKeyAndStatus(key, "PENDING")
        1 * mailService.sendTransferConfirmation("test@example.com", { it.contains("/transfer-confirm?token=") })
        result.transferToken() != null
        result.expiresAt() != null
    }

    def "getStatus throws LicenceTransferNotFoundException for unknown token"() {
        given:
        licenceTransferRepository.findById(_) >> Optional.empty()

        when:
        service.getStatus(UUID.randomUUID())

        then:
        thrown(LicenceTransferNotFoundException)
    }

    def "getStatus returns EXPIRED and deletes entity when TTL passed"() {
        given:
        def token = UUID.randomUUID()
        def now = Instant.now()
        def entity = new LicenceTransferEntity(token, UUID.randomUUID(), "PENDING",
                now.minus(1, ChronoUnit.MINUTES), now.minus(2, ChronoUnit.HOURS))
        licenceTransferRepository.findById(token) >> Optional.of(entity)
        instantProvider.now() >> now

        when:
        def result = service.getStatus(token)

        then:
        result.status() == "EXPIRED"
        result.secret() == null
        1 * licenceTransferRepository.deleteById(token)
    }

    def "getStatus returns PENDING when not yet confirmed"() {
        given:
        def token = UUID.randomUUID()
        def now = Instant.now()
        def entity = new LicenceTransferEntity(token, UUID.randomUUID(), "PENDING",
                now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES))
        licenceTransferRepository.findById(token) >> Optional.of(entity)
        instantProvider.now() >> now

        when:
        def result = service.getStatus(token)

        then:
        result.status() == "PENDING"
        result.secret() == null
    }

    def "getStatus returns CONFIRMED with current licence secret"() {
        given:
        def token = UUID.randomUUID()
        def licenceKey = UUID.randomUUID()
        def now = Instant.now()
        def entity = new LicenceTransferEntity(token, licenceKey, "CONFIRMED",
                now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES))
        def licence = new LicenceEntity()
        licence.key = licenceKey
        licence.secret = "current-rotated-secret"
        licenceTransferRepository.findById(token) >> Optional.of(entity)
        licenceRepository.findByKey(licenceKey) >> Optional.of(licence)
        instantProvider.now() >> now

        when:
        def result = service.getStatus(token)

        then:
        result.status() == "CONFIRMED"
        result.secret() == "current-rotated-secret"
    }

    def "confirm throws LicenceTransferNotFoundException for unknown token"() {
        given:
        licenceTransferRepository.findById(_) >> Optional.empty()

        when:
        service.confirm(UUID.randomUUID())

        then:
        thrown(LicenceTransferNotFoundException)
    }

    def "confirm throws LicenceTransferExpiredException for expired token"() {
        given:
        def token = UUID.randomUUID()
        def now = Instant.now()
        def entity = new LicenceTransferEntity(token, UUID.randomUUID(), "PENDING",
                now.minus(1, ChronoUnit.MINUTES), now.minus(2, ChronoUnit.HOURS))
        licenceTransferRepository.findById(token) >> Optional.of(entity)
        instantProvider.now() >> now

        when:
        service.confirm(token)

        then:
        thrown(LicenceTransferExpiredException)
    }

    def "confirm throws LicenceTransferExpiredException for already-confirmed token"() {
        given:
        def token = UUID.randomUUID()
        def now = Instant.now()
        def entity = new LicenceTransferEntity(token, UUID.randomUUID(), "CONFIRMED",
                now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES))
        licenceTransferRepository.findById(token) >> Optional.of(entity)
        instantProvider.now() >> now

        when:
        service.confirm(token)

        then:
        thrown(LicenceTransferExpiredException)
    }

    def "confirm rotates licence secret, sets status CONFIRMED, returns new secret"() {
        given:
        def token = UUID.randomUUID()
        def licenceKey = UUID.randomUUID()
        def now = Instant.now()
        def entity = new LicenceTransferEntity(token, licenceKey, "PENDING",
                now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES))
        def licence = new LicenceEntity()
        licence.key = licenceKey
        licence.secret = "old-secret"
        licenceTransferRepository.findById(token) >> Optional.of(entity)
        licenceRepository.findByKey(licenceKey) >> Optional.of(licence)
        secureStringProvider.generate(80) >> "new-secret-80chars"
        instantProvider.now() >> now

        when:
        def result = service.confirm(token)

        then:
        result == "new-secret-80chars"
        licence.secret == "new-secret-80chars"
        entity.status == "CONFIRMED"
        1 * licenceRepository.save(licence)
        1 * licenceTransferRepository.save(entity)
    }
}
