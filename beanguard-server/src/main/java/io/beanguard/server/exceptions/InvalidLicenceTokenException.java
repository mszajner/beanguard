package io.beanguard.server.exceptions;

import java.util.UUID;

public class InvalidLicenceTokenException extends RuntimeException {
    public InvalidLicenceTokenException(UUID token) {
        super("Invalid or expired licence token: " + token);
    }
}
