package dev.beanguard.server

import dev.beanguard.api.models.licence.LicenceCreateRequest
import dev.beanguard.server.services.LicenceService
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.ObjectMapper

import java.time.Instant
import java.time.temporal.ChronoUnit

class ShopTokenAcceptanceSpec extends IntegrationSpec {

    @Autowired
    LicenceService licenceService

    OkHttpClient http = new OkHttpClient()
    ObjectMapper mapper = new ObjectMapper()

    def "should generate shop token for valid licence credentials"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .vatId("1234567890")
            .email("shoptoken@example.com")
            .build())

        String credentials = Base64.encoder.encodeToString("${licence.key}:${licence.secret}".bytes)

        when:
        def request = new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/token")
            .post(RequestBody.create(new byte[0], null))
            .header("Authorization", "KeySecret ${credentials}")
            .build()
        def response = http.newCall(request).execute()
        def body = mapper.readTree(response.body().string())

        then:
        response.code() == 200
        body.get("shopUrl") != null
        body.get("shopUrl").asText().contains("token=")
        body.get("expiresAt") != null
    }

    def "should return 404 for invalid credentials"() {
        when:
        String badCredentials = Base64.encoder.encodeToString("00000000-0000-0000-0000-000000000000:wrong".bytes)
        def request = new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/token")
            .post(RequestBody.create(new byte[0], null))
            .header("Authorization", "KeySecret ${badCredentials}")
            .build()
        def response = http.newCall(request).execute()

        then:
        response.code() == 404
    }

    def "should return licence context for valid token"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .vatId("9876543210")
            .email("shopcontext@example.com")
            .companyName("Test Corp")
            .build())

        String credentials = Base64.encoder.encodeToString("${licence.key}:${licence.secret}".bytes)
        def tokenRequest = new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/token")
            .post(RequestBody.create(new byte[0], null))
            .header("Authorization", "KeySecret ${credentials}")
            .build()
        def tokenResponse = mapper.readTree(http.newCall(tokenRequest).execute().body().string())
        def shopUrl = tokenResponse.get("shopUrl").asText()
        def token = shopUrl.substring(shopUrl.indexOf("token=") + 6)

        when:
        def contextRequest = new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/shop/licence?token=${token}")
            .get()
            .build()
        def contextResponse = http.newCall(contextRequest).execute()
        def context = mapper.readTree(contextResponse.body().string())

        then:
        contextResponse.code() == 200
        context.get("email").asText() == "shopcontext@example.com"
        context.get("vatId").asText() == "9876543210"
        context.get("companyName").asText() == "Test Corp"
    }

    def "should return 401 for expired or nonexistent token"() {
        when:
        def request = new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/shop/licence?token=00000000-0000-0000-0000-000000000000")
            .get()
            .build()
        def response = http.newCall(request).execute()

        then:
        response.code() == 401
    }
}
