package dev.beanguard.server.services;

import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;

import java.util.UUID;

public interface LicenceTransferService {
    LicenceTransferInitResponse initiateTransfer(UUID licenceKey);
    LicenceTransferStatusResponse getStatus(UUID token);
    String confirm(UUID token);
}
