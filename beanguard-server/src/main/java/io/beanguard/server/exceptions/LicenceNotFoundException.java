package io.beanguard.server.exceptions;

import java.util.UUID;

public class LicenceNotFoundException extends RuntimeException {
    public LicenceNotFoundException(UUID key) {
        super("Could not find licence " + key);
    }
}
