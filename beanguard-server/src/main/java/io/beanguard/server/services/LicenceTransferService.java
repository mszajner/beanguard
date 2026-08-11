package io.beanguard.server.services;

import io.beanguard.api.models.licence.LicenceTransferInitResponse;
import io.beanguard.api.models.licence.LicenceTransferStatusResponse;

import java.util.UUID;

public interface LicenceTransferService {
    LicenceTransferInitResponse initiateTransfer(UUID licenceKey);
    LicenceTransferStatusResponse getStatus(UUID token);
    String confirm(UUID token);
}
