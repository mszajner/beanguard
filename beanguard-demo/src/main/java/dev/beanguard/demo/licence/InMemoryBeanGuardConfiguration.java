package dev.beanguard.demo.licence;

import dev.beanguard.client.config.LicenceKeys;
import dev.beanguard.client.config.ServerConfig;
import dev.beanguard.demo.config.DemoProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Profile("memory")
@Component
public class InMemoryBeanGuardConfiguration implements DemoLicenceKeyStore {

    private final DemoProperties properties;
    private volatile LicenceKeys licenceKeys;
    private volatile String rawLicence;
    private volatile PendingTransfer pendingTransfer;

    public InMemoryBeanGuardConfiguration(DemoProperties properties) {
        this.properties = properties;
    }

    @Override
    public ServerConfig getServerConfig() {
        DemoProperties.Server server = properties.getServer();
        return new ServerConfig(server.getUrl(), server.getPublicKey(), server.getSecretKey());
    }

    @Override
    public Optional<LicenceKeys> getLicenceKeys() {
        return Optional.ofNullable(licenceKeys);
    }

    @Override
    public void storeLicenceKeys(LicenceKeys keys) {
        this.licenceKeys = keys;
    }

    @Override
    public Optional<String> loadLicence() {
        // Not currently called by beanguard-client; the memory profile loses
        // this on restart by design (that's the point of the profile).
        return Optional.ofNullable(rawLicence);
    }

    @Override
    public void saveLicence(String licence) {
        this.rawLicence = licence;
    }

    @Override
    public void storePendingTransfer(UUID token, UUID licenceKey, Instant expiresAt) {
        this.pendingTransfer = new PendingTransfer(token, licenceKey, expiresAt);
    }

    @Override
    public Optional<PendingTransfer> loadPendingTransfer() {
        return Optional.ofNullable(pendingTransfer);
    }

    @Override
    public void clearPendingTransfer() {
        this.pendingTransfer = null;
    }
}
