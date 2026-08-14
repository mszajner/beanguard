package dev.beanguard.client.registries;

import dev.beanguard.api.models.licence.Licence;

public interface LicenceRegistry {

    Licence getLicence();

    long getLimit(String name);

    LicenceStatus getStatus();

    void refreshLicence();
}
