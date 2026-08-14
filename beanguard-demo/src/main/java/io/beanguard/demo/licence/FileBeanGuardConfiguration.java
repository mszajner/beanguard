package io.beanguard.demo.licence;

import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.config.ServerConfig;
import io.beanguard.demo.config.DemoProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Profile("file")
@Component
public class FileBeanGuardConfiguration implements DemoLicenceKeyStore {

    private final DemoProperties properties;
    private final Path keysFile;
    private final Path licenceFile;
    private final Path pendingTransferFile;

    public FileBeanGuardConfiguration(DemoProperties properties) {
        this.properties = properties;
        Path storagePath = Path.of(properties.getStoragePath());
        this.keysFile = storagePath.resolve("licence-keys.properties");
        this.licenceFile = storagePath.resolve("licence.raw");
        this.pendingTransferFile = storagePath.resolve("pending-transfer.properties");
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ServerConfig getServerConfig() {
        DemoProperties.Server server = properties.getServer();
        return new ServerConfig(server.getUrl(), server.getPublicKey(), server.getSecretKey());
    }

    @Override
    public Optional<LicenceKeys> getLicenceKeys() {
        if (!Files.exists(keysFile)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(keysFile, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String key = props.getProperty("key");
        String secret = props.getProperty("secret");
        if (key == null || secret == null) {
            return Optional.empty();
        }
        return Optional.of(new LicenceKeys(key, secret));
    }

    @Override
    public void storeLicenceKeys(LicenceKeys keys) {
        Properties props = new Properties();
        props.setProperty("key", keys.getKey());
        props.setProperty("secret", keys.getSecret());
        try (var writer = Files.newBufferedWriter(keysFile, StandardCharsets.UTF_8)) {
            props.store(writer, "BeanGuard demo licence keys");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<String> loadLicence() {
        // beanguard-client does not currently call this hook (verified by
        // grep across beanguard-client's sources); implemented honestly in
        // case a future SDK version wires it up for offline fallback.
        if (!Files.exists(licenceFile)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(licenceFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void saveLicence(String licence) {
        try {
            Files.writeString(licenceFile, licence, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void storePendingTransfer(UUID token, UUID licenceKey, Instant expiresAt) {
        Properties props = new Properties();
        props.setProperty("token", token.toString());
        props.setProperty("licenceKey", licenceKey.toString());
        props.setProperty("expiresAt", expiresAt.toString());
        try (var writer = Files.newBufferedWriter(pendingTransferFile, StandardCharsets.UTF_8)) {
            props.store(writer, "BeanGuard demo pending licence transfer");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<PendingTransfer> loadPendingTransfer() {
        if (!Files.exists(pendingTransferFile)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(pendingTransferFile, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String token = props.getProperty("token");
        String licenceKey = props.getProperty("licenceKey");
        String expiresAt = props.getProperty("expiresAt");
        if (token == null || licenceKey == null || expiresAt == null) {
            return Optional.empty();
        }
        return Optional.of(new PendingTransfer(
                UUID.fromString(token), UUID.fromString(licenceKey), Instant.parse(expiresAt)));
    }

    @Override
    public void clearPendingTransfer() {
        try {
            Files.deleteIfExists(pendingTransferFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
