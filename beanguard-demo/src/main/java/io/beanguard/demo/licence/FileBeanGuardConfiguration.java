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
import java.util.Optional;
import java.util.Properties;

@Profile("file")
@Component
public class FileBeanGuardConfiguration implements DemoLicenceKeyStore {

    private final DemoProperties properties;
    private final Path keysFile;
    private final Path licenceFile;

    public FileBeanGuardConfiguration(DemoProperties properties) {
        this.properties = properties;
        Path storagePath = Path.of(properties.getStoragePath());
        this.keysFile = storagePath.resolve("licence-keys.properties");
        this.licenceFile = storagePath.resolve("licence.raw");
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
}
