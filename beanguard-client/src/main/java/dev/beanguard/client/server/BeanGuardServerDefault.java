package dev.beanguard.client.server;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.config.BeanGuardConfiguration;
import dev.beanguard.client.config.LicenceKeys;
import dev.beanguard.client.config.ServerConfig;
import okhttp3.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class BeanGuardServerDefault implements BeanGuardServer {

    private final BeanGuardConfiguration configuration;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();

    private long licenceDecryptorKey;
    private LicenceDecryptor licenceDecryptor = null;

    public BeanGuardServerDefault(BeanGuardConfiguration configuration) {
        this.configuration = configuration;
    }

    public Licence createDemoLicence(LicenceDemoCreateRequest request) throws BeanGuardServerException {
        return decryptLicence(post("/api/open/licences", request, Map.of()));
    }

    public Optional<Licence> getLicence() throws BeanGuardServerException {
        Optional<LicenceKeys> licenceKeys = configuration.getLicenceKeys();
        if (licenceKeys.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(decryptLicence(get("/api/open/licences",
                Map.of("Authorization", "KeySecret "
                        + base64Encoder.encodeToString((licenceKeys.get().getKey() + ":" + licenceKeys.get().getSecret()).getBytes())))));

    }

    private Licence decryptLicence(String encryptedLicence) {
        ServerConfig serverConfig = configuration.getServerConfig();
        long key = serverConfig.getKey().hashCode() + serverConfig.getSecret().hashCode();
        if (licenceDecryptorKey != key) {
            licenceDecryptor = new LicenceDecryptor(serverConfig.getKey(), serverConfig.getSecret());
            licenceDecryptorKey = key;
        }
        return licenceDecryptor.decrypt(encryptedLicence);
    }

    private <T> T get(String method, Map<String, String> headers, TypeReference<T> typeReference)
            throws BeanGuardServerException {
        return objectMapper.readValue(get(method, headers), typeReference);
    }

    private String get(String method, Map<String, String> headers) throws BeanGuardServerException {
        try {
            var request = new Request.Builder()
                    .url(configuration.getServerConfig().getUrl() + method)
                    .headers(Headers.of(headers))
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    throw new RuntimeException("Response is missing");
                }
            }
        } catch (Throwable e) {
            throw new BeanGuardServerException(e.getMessage(), e);
        }
    }

    private <T> T post(String method, Object body, Map<String, String> headers, TypeReference<T> typeReference)
            throws BeanGuardServerException {
        return objectMapper.readValue(post(method, body, headers), typeReference);
    }

    private String post(String method, Object body, Map<String, String> headers) throws BeanGuardServerException {
        try {
            var jsonBody = objectMapper.writeValueAsString(body);
            var requestBody = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            var request = new Request.Builder()
                    .url(configuration.getServerConfig().getUrl() + method)
                    .headers(Headers.of(headers))
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    throw new RuntimeException("Response is missing");
                }
            }
        } catch (Throwable e) {
            throw new BeanGuardServerException(e.getMessage(), e);
        }
    }
}
