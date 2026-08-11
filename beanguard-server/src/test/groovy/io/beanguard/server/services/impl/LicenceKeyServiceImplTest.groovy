package io.beanguard.server.services.impl

import io.beanguard.server.models.ParameterName
import io.beanguard.server.ports.InstantProvider
import io.beanguard.server.services.ParameterService
import spock.lang.Specification

import java.time.Instant

class LicenceKeyServiceImplTest extends Specification {

    def parameterService = Mock(ParameterService)
    def instantProvider = Mock(InstantProvider)
    def keyMaterialGenerator = new KeyMaterialGenerator()
    def service = new LicenceKeyServiceImpl(parameterService, instantProvider, keyMaterialGenerator)

    def "regenerateKeys generates and saves a new RSA key pair and AES secret key"() {
        given:
        def now = Instant.parse("2026-07-24T10:00:00Z")
        instantProvider.now() >> now

        when:
        def result = service.regenerateKeys()

        then:
        1 * parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY, { it.length() > 0 })
        1 * parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, { it.length() > 0 })
        1 * parameterService.setString(ParameterName.LICENCE_SECRET_KEY, { it.length() > 0 })
        result.publicKey().length() > 0
        result.secretKey().length() > 0
        result.rotatedAt() == now
    }

    def "regenerateKeys returns a different key pair on each call"() {
        given:
        instantProvider.now() >> Instant.now()

        when:
        def first = service.regenerateKeys()
        def second = service.regenerateKeys()

        then:
        first.publicKey() != second.publicKey()
        first.secretKey() != second.secretKey()
    }

    def "regenerateKeys never exposes the private key in its result"() {
        given:
        instantProvider.now() >> Instant.now()
        def capturedPrivateKey = null
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, _) >> { args -> capturedPrivateKey = args[1] }

        when:
        def result = service.regenerateKeys()

        then:
        capturedPrivateKey != null
        result.publicKey() != capturedPrivateKey
        result.secretKey() != capturedPrivateKey
    }
}
