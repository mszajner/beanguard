package dev.beanguard.server

import dev.beanguard.api.models.licence.LicenceCreateRequest
import dev.beanguard.server.services.LicenceService
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

import java.time.Instant
import java.time.temporal.ChronoUnit

/** Shared helpers for specs exercising the open licence-transfer endpoints end to end. */
abstract class TransferFlowSpec extends IntegrationSpec {

    static final MediaType JSON = MediaType.get("application/json")

    @Autowired LicenceService licenceService

    OkHttpClient http = new OkHttpClient()
    ObjectMapper mapper = new ObjectMapper()

    private String url(String path) { "http://localhost:${localServerPort}${path}" }

    def newLicence(String email) {
        licenceService.createLicence(LicenceCreateRequest.builder()
                .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
                .vatId("1234567890")
                .email(email)
                .build())
    }

    /** @return [code, body] */
    def initiate(Object licenceKey, Map headers = [:]) {
        call(new Request.Builder().url(url("/api/open/licences/transfer"))
                .headers(okhttp3.Headers.of(headers))
                .post(RequestBody.create("""{"licenceKey":"${licenceKey}"}""", JSON)))
    }

    def poll(Object token) {
        call(new Request.Builder().url(url("/api/open/licences/transfer/${token}")).get())
    }

    def confirm(Object token) {
        call(new Request.Builder().url(url("/api/open/licences/transfer/${token}/confirm"))
                .post(RequestBody.create(new byte[0], null)))
    }

    def licenceDetailsCode(Object key, String secret) {
        def creds = Base64.encoder.encodeToString("${key}:${secret}".bytes)
        http.newCall(new Request.Builder().url(url("/api/open/licences"))
                .header("Authorization", "KeySecret ${creds}").get().build()).execute().code()
    }

    private def call(Request.Builder builder) {
        def response = http.newCall(builder.build()).execute()
        def text = response.body().string()
        [code: response.code(), body: text ? mapper.readTree(text) : null]
    }

    /** The confirmation token from the link in the email sent to {@code to}, or null if no email arrived. */
    String mailedConfirmToken(String to) {
        def base = "http://${mailpit.getHost()}:${mailpit.getMappedPort(8025)}"
        for (int i = 0; i < 20; i++) {
            JsonNode search = mapper.readTree(http.newCall(new Request.Builder()
                    .url("${base}/api/v1/search?query=to:${to}").get().build()).execute().body().string())
            def messages = search.get("messages")
            if (messages != null && messages.size() > 0) {
                def id = messages.get(0).get("ID").asText()
                def message = mapper.readTree(http.newCall(new Request.Builder()
                        .url("${base}/api/v1/message/${id}").get().build()).execute().body().string())
                def matcher = (message.get("HTML").asText() =~ /transfer-confirm\?token=([0-9a-zA-Z-]+)/)
                return matcher.find() ? matcher.group(1) : null
            }
            Thread.sleep(100)
        }
        return null
    }

    int mailCount(String to) {
        def base = "http://${mailpit.getHost()}:${mailpit.getMappedPort(8025)}"
        mapper.readTree(http.newCall(new Request.Builder()
                .url("${base}/api/v1/search?query=to:${to}").get().build()).execute().body().string())
                .get("messages").size()
    }
}
