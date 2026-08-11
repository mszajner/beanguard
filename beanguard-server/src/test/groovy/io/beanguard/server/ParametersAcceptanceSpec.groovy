package io.beanguard.server

import io.beanguard.server.controllers.ParametersController
import io.beanguard.server.models.ParameterName
import io.beanguard.server.models.ParameterUpdateRequest
import io.beanguard.server.services.ParameterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.server.ResponseStatusException

class ParametersAcceptanceSpec extends IntegrationSpec {

    @Autowired ParametersController parametersController
    @Autowired ParameterService parameterService

    def "getAllParameters returns all parameters"() {
        when:
        def result = parametersController.getAllParameters()

        then:
        result.size() == ParameterName.values().length
        result.every { it.name != null }
    }

    def "getAllParameters masks private keys as null"() {
        when:
        def result = parametersController.getAllParameters()

        then:
        def privateKeys = result.findAll { it.name.endsWith("_PRIVATE_KEY") }
        privateKeys.size() == 2
        privateKeys.every { it.value == null }
    }

    def "getAllParameters returns non-null value for secret keys"() {
        when:
        def result = parametersController.getAllParameters()

        then:
        def secretKeys = result.findAll { it.name.endsWith("_SECRET_KEY") }
        secretKeys.size() == 2
        secretKeys.every { it.value != null }
    }

    def "getAllParameters returns non-null value for public keys"() {
        when:
        def result = parametersController.getAllParameters()

        then:
        def publicKey = result.find { it.name == "LICENCE_PUBLIC_KEY" }
        publicKey.value != null
    }

    def "updateParameter updates the value and returns updated record"() {
        given:
        def request = new ParameterUpdateRequest("NEW_ISSUER")

        when:
        def result = parametersController.updateParameter("LICENCE_ISSUER", request)

        then:
        result.name == "LICENCE_ISSUER"
        result.value == "NEW_ISSUER"
        result.updatedAt != null
    }

    def "updateParameter throws 400 for unknown parameter name"() {
        given:
        def request = new ParameterUpdateRequest("anything")

        when:
        parametersController.updateParameter("UNKNOWN_PARAM", request)

        then:
        def ex = thrown(ResponseStatusException)
        ex.statusCode.value() == 400
    }

    def "updateParameter for write-only parameter returns null value"() {
        given:
        def originalValue = parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY)
        def request = new ParameterUpdateRequest("some-new-key-value")

        when:
        def result = parametersController.updateParameter("LICENCE_PRIVATE_KEY", request)

        then:
        result.name == "LICENCE_PRIVATE_KEY"
        result.value == null

        cleanup:
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, originalValue)
    }
}
