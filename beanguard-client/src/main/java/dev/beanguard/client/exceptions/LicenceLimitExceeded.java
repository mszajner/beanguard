package dev.beanguard.client.exceptions;

public final class LicenceLimitExceeded extends RuntimeException {
    public LicenceLimitExceeded(String message) {
        super(message);
    }
}
