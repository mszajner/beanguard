package dev.beanguard.server

import dev.beanguard.server.models.ParameterName
import dev.beanguard.server.security.TokenEncryptor
import dev.beanguard.server.services.ParameterService
import dev.beanguard.server.services.TokenKeyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User

class TokenKeyRotationAcceptanceSpec extends IntegrationSpec {

    @Autowired TokenEncryptor tokenEncryptor
    @Autowired TokenKeyService tokenKeyService
    @Autowired ParameterService parameterService

    def "rotating token keys immediately invalidates tokens issued with the old keys, without a restart"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.TOKEN_SECRET_KEY)

        def userDetails = User.withUsername("admin@beanguard.dev").password("x").roles("ADMIN").build()
        def tokenBeforeRotation = tokenEncryptor.generateToken(userDetails)

        // sanity check: validates fine BEFORE rotation, in this same running instance
        assert tokenEncryptor.validateToken(tokenBeforeRotation, userDetails)

        when:
        tokenKeyService.regenerateKeys()
        tokenEncryptor.validateToken(tokenBeforeRotation, userDetails)

        then:
        thrown(Exception)

        cleanup:
        parameterService.setString(ParameterName.TOKEN_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.TOKEN_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.TOKEN_SECRET_KEY, originalSecretKey)
    }

    def "rotating token keys produces new keys that correctly generate and validate tokens"() {
        given:
        def originalPublicKey = parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY)
        def originalPrivateKey = parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)
        def originalSecretKey = parameterService.getString(ParameterName.TOKEN_SECRET_KEY)

        def userDetails = User.withUsername("admin@beanguard.dev").password("x").roles("ADMIN").build()

        when:
        tokenKeyService.regenerateKeys()
        def newToken = tokenEncryptor.generateToken(userDetails)

        then:
        tokenEncryptor.validateToken(newToken, userDetails)

        cleanup:
        parameterService.setString(ParameterName.TOKEN_PUBLIC_KEY, originalPublicKey)
        parameterService.setString(ParameterName.TOKEN_PRIVATE_KEY, originalPrivateKey)
        parameterService.setString(ParameterName.TOKEN_SECRET_KEY, originalSecretKey)
    }
}
