package io.beanguard.client.server;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.api.models.licence.LicenceDemoCreateRequest;

import java.util.Optional;

public interface BeanGuardServer {

    Licence createDemoLicence(LicenceDemoCreateRequest request) throws BeanGuardServerException;

    Optional<Licence> getLicence() throws BeanGuardServerException;
}
