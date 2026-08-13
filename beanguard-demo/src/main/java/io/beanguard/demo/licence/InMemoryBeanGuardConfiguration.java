package io.beanguard.demo.licence;

import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.config.ServerConfig;
import io.beanguard.demo.config.DemoProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile("memory")
@Component
public class InMemoryBeanGuardConfiguration implements DemoLicenceKeyStore {

    private final DemoProperties properties;
    private volatile LicenceKeys licenceKeys;
    private volatile String rawLicence;

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
}
