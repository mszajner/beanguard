package dev.beanguard.client.server;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;

import java.util.Optional;
import java.util.UUID;

public interface BeanGuardServer {

    Licence createDemoLicence(LicenceDemoCreateRequest request) throws BeanGuardServerException;

    Optional<Licence> getLicence() throws BeanGuardServerException;

    /**
     * Starts transferring a licence to this machine. The server sends a confirmation link to the licence
     * owner; once it is opened, {@link #getLicenceTransferStatus(UUID)} returns the new licence secret.
     */
    LicenceTransferInitResponse initiateLicenceTransfer(UUID licenceKey) throws BeanGuardServerException;

    /**
     * Polls a transfer started with {@link #initiateLicenceTransfer(UUID)}. The status is {@code PENDING},
     * {@code CONFIRMED} (the response then carries the new secret) or {@code EXPIRED}.
     */
    LicenceTransferStatusResponse getLicenceTransferStatus(UUID transferToken) throws BeanGuardServerException;
}
