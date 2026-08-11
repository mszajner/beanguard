package io.beanguard.server

import io.beanguard.server.models.ParameterName
import io.beanguard.server.services.ParameterService
import io.beanguard.server.services.impl.ServerInitializationService
import org.springframework.beans.factory.annotation.Autowired

import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class ServerInitializationAcceptanceSpec extends IntegrationSpec {

    @Autowired
    ParameterService parameterService

    @Autowired
    ServerInitializationService serverInitializationService

    def "should auto-generate all six crypto keys on first start"() {
        expect:
        !parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY).isEmpty()
        !parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY).isEmpty()
        !parameterService.getString(ParameterName.LICENCE_SECRET_KEY).isEmpty()
        !parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY).isEmpty()
        !parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY).isEmpty()
        !parameterService.getString(ParameterName.TOKEN_SECRET_KEY).isEmpty()
    }

    def "should generate valid RSA key pairs for LICENCE and TOKEN"() {
        given:
        def keyFactory = KeyFactory.getInstance("RSA")

        when:
        def licencePublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(
                Base64.decoder.decode(parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY))))
        def licencePrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(
                Base64.decoder.decode(parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY))))
        def tokenPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(
                Base64.decoder.decode(parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY))))
        def tokenPrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(
                Base64.decoder.decode(parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY))))

        then:
        licencePublicKey.algorithm == "RSA"
        licencePrivateKey.algorithm == "RSA"
        tokenPublicKey.algorithm == "RSA"
        tokenPrivateKey.algorithm == "RSA"
    }

    def "should generate 256-bit AES keys for LICENCE and TOKEN"() {
        when:
        def licenceSecretBytes = Base64.decoder.decode(parameterService.getString(ParameterName.LICENCE_SECRET_KEY))
        def tokenSecretBytes   = Base64.decoder.decode(parameterService.getString(ParameterName.TOKEN_SECRET_KEY))

        then:
        licenceSecretBytes.length == 32
        tokenSecretBytes.length == 32
    }

    def "should not overwrite existing keys when run is called again"() {
        given:
        def originalLicencePrivKey  = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        def originalLicencePubKey   = parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY)
        def originalLicenceSecret   = parameterService.getString(ParameterName.LICENCE_SECRET_KEY)
        def originalTokenPrivKey    = parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)
        def originalTokenPubKey     = parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY)
        def originalTokenSecret     = parameterService.getString(ParameterName.TOKEN_SECRET_KEY)

        when:
        serverInitializationService.run(null)

        then:
        parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY) == originalLicencePrivKey
        parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY)  == originalLicencePubKey
        parameterService.getString(ParameterName.LICENCE_SECRET_KEY)  == originalLicenceSecret
        parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY)   == originalTokenPrivKey
        parameterService.getString(ParameterName.TOKEN_PUBLIC_KEY)    == originalTokenPubKey
        parameterService.getString(ParameterName.TOKEN_SECRET_KEY)    == originalTokenSecret
    }
}
