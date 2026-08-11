package io.beanguard.server

import io.beanguard.server.controllers.TokenKeysController
import io.beanguard.server.models.ParameterName
import io.beanguard.server.services.ParameterService
import org.springframework.beans.factory.annotation.Autowired

class TokenKeysAcceptanceSpec extends IntegrationSpec {

    @Autowired TokenKeysController tokenKeysController
    @Autowired ParameterService parameterService

    def "regenerate creates and persists a new key pair and secret key"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.TOKEN_SECRET_KEY)

        when:
        def result = tokenKeysController.regenerate()

        then:
        result.publicKey() != null
        result.secretKey() != null
        result.rotatedAt() != null
        result.publicKey() != originalPublicKey
        parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY) == result.publicKey()

        cleanup:
        parameterService.setString(ParameterName.TOKEN_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.TOKEN_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.TOKEN_SECRET_KEY, originalSecretKey)
    }

    def "regenerate does not expose the private key in the response"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.TOKEN_SECRET_KEY)

        when:
        def result = tokenKeysController.regenerate()

        then:
        def privateKey = parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)
        result.publicKey() != privateKey
        result.secretKey() != privateKey

        cleanup:
        parameterService.setString(ParameterName.TOKEN_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.TOKEN_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.TOKEN_SECRET_KEY, originalSecretKey)
    }
}
