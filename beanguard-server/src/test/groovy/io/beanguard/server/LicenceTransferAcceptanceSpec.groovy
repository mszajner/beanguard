package io.beanguard.server

import io.beanguard.api.models.licence.LicenceCreateRequest
import io.beanguard.server.services.LicenceService
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.ObjectMapper

import java.time.Instant
import java.time.temporal.ChronoUnit

class LicenceTransferAcceptanceSpec extends IntegrationSpec {

    @Autowired LicenceService licenceService

    OkHttpClient http = new OkHttpClient()
    ObjectMapper mapper = new ObjectMapper()
    static final MediaType JSON = MediaType.get("application/json")

    def "happy path: initiate -> poll PENDING -> confirm -> poll CONFIRMED with new secret"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .vatId("1234567890")
            .email("transfer@example.com")
            .build())
        def oldSecret = licence.secret

        when: "initiate transfer"
        def initResp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer")
            .post(RequestBody.create("""{"licenceKey":"${licence.key}"}""", JSON))
            .build()).execute()
        def initBody = mapper.readTree(initResp.body().string())

        then:
        initResp.code() == 200
        def transferToken = initBody.get("transferToken").asText()
        transferToken != null && !transferToken.isBlank()
        initBody.get("expiresAt") != null

        when: "poll — should be PENDING"
        def pendingResp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${transferToken}")
            .get().build()).execute()
        def pendingBody = mapper.readTree(pendingResp.body().string())

        then:
        pendingResp.code() == 200
        pendingBody.get("status").asText() == "PENDING"
        pendingBody.get("secret") == null

        when: "confirm transfer"
        def confirmResp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${transferToken}/confirm")
            .post(RequestBody.create(new byte[0], null))
            .build()).execute()
        def confirmBody = mapper.readTree(confirmResp.body().string())

        then:
        confirmResp.code() == 200
        def newSecret = confirmBody.get("secret").asText()
        newSecret != null && !newSecret.isBlank()
        newSecret != oldSecret

        when: "poll again — should be CONFIRMED with secret"
        def confirmedResp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${transferToken}")
            .get().build()).execute()
        def confirmedBody = mapper.readTree(confirmedResp.body().string())

        then:
        confirmedResp.code() == 200
        confirmedBody.get("status").asText() == "CONFIRMED"
        confirmedBody.get("secret").asText() == newSecret
    }

    def "old credentials fail after transfer, new credentials succeed"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .vatId("9876543210")
            .email("transfer2@example.com")
            .build())
        def oldCreds = Base64.encoder.encodeToString("${licence.key}:${licence.secret}".bytes)

        // initiate + confirm transfer
        def token = mapper.readTree(http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer")
            .post(RequestBody.create("""{"licenceKey":"${licence.key}"}""", JSON))
            .build()).execute().body().string()).get("transferToken").asText()
        def newSecret = mapper.readTree(http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${token}/confirm")
            .post(RequestBody.create(new byte[0], null))
            .build()).execute().body().string()).get("secret").asText()

        when: "old credentials"
        def oldResp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences")
            .header("Authorization", "KeySecret ${oldCreds}")
            .get().build()).execute()

        then:
        oldResp.code() == 404

        when: "new credentials"
        def newCreds = Base64.encoder.encodeToString("${licence.key}:${newSecret}".bytes)
        def newResp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences")
            .header("Authorization", "KeySecret ${newCreds}")
            .get().build()).execute()

        then:
        newResp.code() == 200
    }

    def "second initiate replaces first transfer token"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .vatId("1111111111")
            .email("transfer3@example.com")
            .build())

        def firstToken = mapper.readTree(http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer")
            .post(RequestBody.create("""{"licenceKey":"${licence.key}"}""", JSON))
            .build()).execute().body().string()).get("transferToken").asText()

        when: "second initiate"
        def secondToken = mapper.readTree(http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer")
            .post(RequestBody.create("""{"licenceKey":"${licence.key}"}""", JSON))
            .build()).execute().body().string()).get("transferToken").asText()

        then:
        secondToken != firstToken

        and: "first token is gone"
        http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${firstToken}")
            .get().build()).execute().code() == 404
    }

    def "confirm already-confirmed token returns 410"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .vatId("2222222222")
            .email("transfer4@example.com")
            .build())
        def token = mapper.readTree(http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer")
            .post(RequestBody.create("""{"licenceKey":"${licence.key}"}""", JSON))
            .build()).execute().body().string()).get("transferToken").asText()
        http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${token}/confirm")
            .post(RequestBody.create(new byte[0], null))
            .build()).execute()

        when:
        def resp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/${token}/confirm")
            .post(RequestBody.create(new byte[0], null))
            .build()).execute()

        then:
        resp.code() == 410
    }

    def "initiate with unknown licence key returns 404"() {
        when:
        def resp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer")
            .post(RequestBody.create('{"licenceKey":"00000000-0000-0000-0000-000000000000"}', JSON))
            .build()).execute()

        then:
        resp.code() == 404
    }

    def "poll unknown token returns 404"() {
        when:
        def resp = http.newCall(new Request.Builder()
            .url("http://localhost:${localServerPort}/api/open/licences/transfer/00000000-0000-0000-0000-000000000000")
            .get().build()).execute()

        then:
        resp.code() == 404
    }
}
