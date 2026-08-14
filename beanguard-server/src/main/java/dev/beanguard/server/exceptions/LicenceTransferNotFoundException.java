package dev.beanguard.server.exceptions;

import java.util.UUID;

public class LicenceTransferNotFoundException extends RuntimeException {
    public LicenceTransferNotFoundException(UUID token) {
        super("Transfer request not found: " + token);
    }
}
