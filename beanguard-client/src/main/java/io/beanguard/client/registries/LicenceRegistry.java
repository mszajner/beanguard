package io.beanguard.client.registries;

import io.beanguard.api.models.licence.Licence;

public interface LicenceRegistry {

    Licence getLicence();

    long getLimit(String name);

    LicenceStatus getStatus();

    void refreshLicence();
}
