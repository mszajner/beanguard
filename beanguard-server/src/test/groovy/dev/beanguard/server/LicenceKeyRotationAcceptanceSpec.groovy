package dev.beanguard.server

import dev.beanguard.api.models.licence.Licence
import dev.beanguard.server.models.ParameterName
import dev.beanguard.server.services.LicenceKeyService
import dev.beanguard.server.services.ParameterService
import dev.beanguard.server.services.impl.LicenceEncryptor
import org.springframework.beans.factory.annotation.Autowired

import java.time.Instant
import java.time.temporal.ChronoUnit

class LicenceKeyRotationAcceptanceSpec extends IntegrationSpec {

    @Autowired LicenceEncryptor licenceEncryptor
    @Autowired LicenceKeyService licenceKeyService
    @Autowired ParameterService parameterService

    def "rotating licence keys immediately invalidates licences encrypted with the old keys, without a restart"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.LICENCE_SECRET_KEY)

        def licence = Licence.builder()
                .key(UUID.randomUUID())
                .secret("test-secret")
                .expiration(Instant.now().plus(30, ChronoUnit.DAYS))
                .updatedAt(Instant.now())
                .build()
        def encryptedBeforeRotation = licenceEncryptor.encryptLicence(licence)

        // sanity check: decrypts fine BEFORE rotation, in this same running instance
        assert licenceEncryptor.decryptLicence(encryptedBeforeRotation).key == licence.key

        when:
        licenceKeyService.regenerateKeys()
        licenceEncryptor.decryptLicence(encryptedBeforeRotation)

        then:
        thrown(Exception)

        cleanup:
        parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.LICENCE_SECRET_KEY, originalSecretKey)
    }

    def "rotating licence keys produces new keys that correctly encrypt and decrypt licences"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.LICENCE_SECRET_KEY)

        def newLicence = Licence.builder()
                .key(UUID.randomUUID())
                .secret("test-secret")
                .expiration(Instant.now().plus(30, ChronoUnit.DAYS))
                .updatedAt(Instant.now())
                .build()

        when:
        licenceKeyService.regenerateKeys()

        then:
        licenceEncryptor.decryptLicence(licenceEncryptor.encryptLicence(newLicence)).key == newLicence.key

        cleanup:
        parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.LICENCE_SECRET_KEY, originalSecretKey)
    }
}
