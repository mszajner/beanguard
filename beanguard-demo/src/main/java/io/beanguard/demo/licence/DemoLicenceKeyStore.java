package io.beanguard.demo.licence;

import io.beanguard.client.config.BeanGuardConfiguration;
import io.beanguard.client.config.LicenceKeys;

public interface DemoLicenceKeyStore extends BeanGuardConfiguration {

    void storeLicenceKeys(LicenceKeys keys);
}
