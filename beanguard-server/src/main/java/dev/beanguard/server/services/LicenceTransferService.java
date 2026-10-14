package dev.beanguard.server.services;

import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;

import java.util.UUID;

public interface LicenceTransferService {

    /**
     * Starts a transfer and emails the confirmation link to the licence owner. The returned token only
     * reads the outcome; it cannot confirm. The response is the same whether or not the key exists, has
     * an email, or is inside the cooldown, so it reveals nothing about licences.
     */
    LicenceTransferInitResponse initiateTransfer(UUID licenceKey);

    /**
     * Polls a transfer by the token returned from {@link #initiateTransfer}. The first poll after the
     * owner confirmed rotates the licence secret and returns it; the transfer is then consumed.
     */
    LicenceTransferStatusResponse getStatus(UUID token);

    /** Confirms a transfer by the token from the emailed link. Does not reveal or rotate the secret. */
    void confirm(UUID confirmToken);
}
