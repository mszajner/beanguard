package io.beanguard.server

import io.beanguard.server.controllers.LicenceKeysController
import io.beanguard.server.models.ParameterName
import io.beanguard.server.services.ParameterService
import org.springframework.beans.factory.annotation.Autowired

class LicenceKeysAcceptanceSpec extends IntegrationSpec {

    @Autowired LicenceKeysController licenceKeysController
    @Autowired ParameterService parameterService

    def "regenerate creates and persists a new key pair and secret key"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.LICENCE_SECRET_KEY)

        when:
        def result = licenceKeysController.regenerate()

        then:
        result.publicKey() != null
        result.secretKey() != null
        result.rotatedAt() != null
        result.publicKey() != originalPublicKey
        parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY) == result.publicKey()

        cleanup:
        parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.LICENCE_SECRET_KEY, originalSecretKey)
    }

    def "regenerate does not expose the private key in the response"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.LICENCE_SECRET_KEY)

        when:
        def result = licenceKeysController.regenerate()

        then:
        def privateKey = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        result.publicKey() != privateKey
        result.secretKey() != privateKey

        cleanup:
        parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.LICENCE_SECRET_KEY, originalSecretKey)
    }
}
