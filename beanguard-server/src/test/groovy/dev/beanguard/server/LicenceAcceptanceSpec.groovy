package dev.beanguard.server

import dev.beanguard.api.models.licence.LicenceDemoCreateRequest
import dev.beanguard.client.config.BeanGuardConfiguration
import dev.beanguard.client.config.LicenceKeys
import dev.beanguard.client.config.ServerConfig
import dev.beanguard.client.server.BeanGuardServer
import dev.beanguard.client.server.BeanGuardServerDefault
import dev.beanguard.server.models.ParameterName
import dev.beanguard.server.services.ParameterService
import org.springframework.beans.factory.annotation.Autowired

class LicenceAcceptanceSpec extends IntegrationSpec {

    private BeanGuardServer licencesClient
    private TestServerConfiguration serverConfiguration

    @Autowired
    private ParameterService parameterService

    def setup() {
        serverConfiguration = new TestServerConfiguration(
                "http://localhost:" + localServerPort,
                parameterService.getString(ParameterName.LICENCE_PUBLIC_KEY),
                parameterService.getString(ParameterName.LICENCE_SECRET_KEY))
        licencesClient = new BeanGuardServerDefault(serverConfiguration)
    }

    def "user should issue demo licence and fetch it back using its own key and secret"() {
        given: "user entered email and vatId"
            def email = "demo-" + System.currentTimeMillis() + "@rexoft.pl"
            def vatId = "5632103754"
        when: "user requested demo licence"
            def licence = licencesClient.createDemoLicence(
                    LicenceDemoCreateRequest.builder()
                        .email(email)
                        .vatId(vatId)
                        .build())
        then: "demo licence is returned"
            licence.getEmail() == email
            licence.getVatId() == vatId
            licence.getClaims() == [ users: "1" ]

        when: "licence is requested again using its own key and secret"
            serverConfiguration.setLicenceKeys(licence.getKey().toString(), licence.getSecret())
            def requestedLicence = licencesClient.getLicence()
        then: "licence is returned"
            requestedLicence.get() == licence
    }

    private static class TestServerConfiguration implements BeanGuardConfiguration {
        private final String url
        private final String licencePublicKey
        private final String licenceSecretKey
        private String key = ""
        private String secret = ""

        TestServerConfiguration(String url, String licencePublicKey, String licenceSecretKey) {
            this.url = url
            this.licencePublicKey = licencePublicKey
            this.licenceSecretKey = licenceSecretKey
        }

        void setLicenceKeys(String key, String secret) {
            this.key = key
            this.secret = secret
        }

        @Override
        ServerConfig getServerConfig() {
            new ServerConfig(url, licencePublicKey, licenceSecretKey)
        }

        @Override
        Optional<LicenceKeys> getLicenceKeys() {
            Optional.of(new LicenceKeys(key, secret))
        }

        @Override
        Optional<String> loadLicence() {
            Optional.empty()
        }

        @Override
        void saveLicence(String licence) {

        }
    }
}
