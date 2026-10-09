package dev.beanguard.client.server;

import com.sun.net.httpserver.HttpServer;
import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;
import dev.beanguard.client.config.BeanGuardConfiguration;
import dev.beanguard.client.config.LicenceKeys;
import dev.beanguard.client.config.ServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeanGuardServerDefaultTest {

    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();

    private int responseCode = 200;
    private String responseBody = "{}";
    private HttpServer httpServer;
    private BeanGuardServerDefault server;

    @BeforeEach
    void startServer() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.createContext("/", exchange -> {
            method.set(exchange.getRequestMethod());
            path.set(exchange.getRequestURI().getPath());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseCode, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        httpServer.start();
        String url = "http://localhost:" + httpServer.getAddress().getPort();
        server = new BeanGuardServerDefault(new BeanGuardConfiguration() {
            public ServerConfig getServerConfig() {
                return new ServerConfig(url, "public", "secret");
            }

            public Optional<LicenceKeys> getLicenceKeys() {
                return Optional.empty();
            }

            public Optional<String> loadLicence() {
                return Optional.empty();
            }

            public void saveLicence(String licence) {
            }
        });
    }

    @AfterEach
    void stopServer() {
        httpServer.stop(0);
    }

    @Test
    void initiateLicenceTransferPostsLicenceKeyWithoutAuthorization() throws Exception {
        UUID licenceKey = UUID.randomUUID();
        UUID transferToken = UUID.randomUUID();
        responseBody = "{\"transferToken\":\"" + transferToken + "\",\"expiresAt\":\"2030-01-01T10:00:00Z\"}";

        LicenceTransferInitResponse response = server.initiateLicenceTransfer(licenceKey);

        assertThat(method.get()).isEqualTo("POST");
        assertThat(path.get()).isEqualTo("/api/open/licences/transfer");
        assertThat(requestBody.get()).contains("\"licenceKey\":\"" + licenceKey + "\"");
        assertThat(authorization.get()).isNull();
        assertThat(response.transferToken()).isEqualTo(transferToken);
        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2030-01-01T10:00:00Z"));
    }

    @Test
    void getLicenceTransferStatusReturnsConfirmedStatusWithSecret() throws Exception {
        UUID transferToken = UUID.randomUUID();
        responseBody = "{\"status\":\"CONFIRMED\",\"secret\":\"new-secret\"}";

        LicenceTransferStatusResponse response = server.getLicenceTransferStatus(transferToken);

        assertThat(method.get()).isEqualTo("GET");
        assertThat(path.get()).isEqualTo("/api/open/licences/transfer/" + transferToken);
        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.secret()).isEqualTo("new-secret");
    }

    @Test
    void getLicenceTransferStatusReturnsPendingStatusWithoutSecret() throws Exception {
        responseBody = "{\"status\":\"PENDING\"}";

        LicenceTransferStatusResponse response = server.getLicenceTransferStatus(UUID.randomUUID());

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.secret()).isNull();
    }

    @Test
    void serverErrorIsReportedAsBeanGuardServerException() {
        responseCode = 404;
        responseBody = "transfer not found";

        assertThatThrownBy(() -> server.getLicenceTransferStatus(UUID.randomUUID()))
                .isInstanceOf(BeanGuardServerException.class)
                .hasMessageContaining("transfer not found");
        assertThatThrownBy(() -> server.initiateLicenceTransfer(UUID.randomUUID()))
                .isInstanceOf(BeanGuardServerException.class)
                .hasMessageContaining("transfer not found");
    }
}
