package dev.beanguard.server.exceptions;

import java.util.UUID;

public class LicenceTransferExpiredException extends RuntimeException {
    public LicenceTransferExpiredException(UUID token) {
        super("Transfer request expired or already processed: " + token);
    }
}
