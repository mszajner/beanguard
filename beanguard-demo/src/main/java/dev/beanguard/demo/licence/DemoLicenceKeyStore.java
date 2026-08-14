package dev.beanguard.demo.licence;

import dev.beanguard.client.config.BeanGuardConfiguration;
import dev.beanguard.client.config.LicenceKeys;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DemoLicenceKeyStore extends BeanGuardConfiguration {

    void storeLicenceKeys(LicenceKeys keys);

    void storePendingTransfer(UUID token, UUID licenceKey, Instant expiresAt);

    Optional<PendingTransfer> loadPendingTransfer();

    void clearPendingTransfer();

    record PendingTransfer(UUID token, UUID licenceKey, Instant expiresAt) {
    }
}
