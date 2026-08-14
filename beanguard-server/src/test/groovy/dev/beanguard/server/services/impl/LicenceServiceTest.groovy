package dev.beanguard.server.services.impl

import dev.beanguard.api.models.licence.Licence
import dev.beanguard.api.models.licence.LicenceCreateRequest
import dev.beanguard.api.models.licence.LicenceType
import dev.beanguard.server.entities.LicenceEntity
import dev.beanguard.server.mappers.LicenceMapper
import dev.beanguard.server.ports.InstantProvider
import dev.beanguard.server.ports.SecureStringProvider
import dev.beanguard.server.repositories.LicenceRepository
import dev.beanguard.server.repositories.ProductRepository
import dev.beanguard.server.services.CertificateService
import dev.beanguard.server.services.MailService
import dev.beanguard.server.services.ParameterService
import spock.lang.Specification

class LicenceServiceTest extends Specification {

    def licenceRepository = Mock(LicenceRepository)
    def productRepository = Mock(ProductRepository)
    def licenceMapper = Mock(LicenceMapper)
    def licenceEncryptor = Mock(LicenceEncryptor)
    def secureStringProvider = Mock(SecureStringProvider)
    def instantProvider = Mock(InstantProvider)
    def parameterService = Mock(ParameterService)
    def mailService = Mock(MailService)
    def certificateService = Mock(CertificateService)
    def service = new LicenceServiceImpl(
            licenceRepository, productRepository, licenceMapper, licenceEncryptor,
            secureStringProvider, instantProvider, parameterService, mailService, certificateService)

    def "createLicence sends welcome email for STANDARD licence with email"() {
        given:
        def email = "vendor@example.com"
        def licenceKey = UUID.randomUUID()
        def request = LicenceCreateRequest.builder()
                .vatId("1234567890")
                .email(email)
                .type(LicenceType.STANDARD)
                .build()
        def entity = new LicenceEntity()
        entity.email = email
        entity.type = LicenceType.STANDARD
        def savedEntity = new LicenceEntity()
        savedEntity.key = licenceKey
        savedEntity.email = email
        savedEntity.type = LicenceType.STANDARD
        licenceMapper.toEntity(request) >> entity
        secureStringProvider.generate(80) >> "secret80"
        licenceRepository.save(entity) >> savedEntity
        licenceMapper.toDto(savedEntity) >> Mock(Licence)

        when:
        service.createLicence(request)

        then:
        1 * mailService.sendLicenceCreated(email, licenceKey)
    }

    def "createLicence does NOT send email for DEMO licence"() {
        given:
        def email = "demo@example.com"
        def licenceKey = UUID.randomUUID()
        def request = LicenceCreateRequest.builder()
                .vatId("1234567890")
                .email(email)
                .type(LicenceType.DEMO)
                .build()
        def entity = new LicenceEntity()
        entity.email = email
        entity.type = LicenceType.DEMO
        def savedEntity = new LicenceEntity()
        savedEntity.key = licenceKey
        savedEntity.email = email
        savedEntity.type = LicenceType.DEMO
        licenceMapper.toEntity(request) >> entity
        secureStringProvider.generate(80) >> "secret80"
        licenceRepository.save(entity) >> savedEntity
        licenceMapper.toDto(savedEntity) >> Mock(Licence)

        when:
        service.createLicence(request)

        then:
        0 * mailService.sendLicenceCreated(_, _)
    }
}
