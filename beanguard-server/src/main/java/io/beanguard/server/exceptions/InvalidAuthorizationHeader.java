package io.beanguard.server.exceptions;

public class InvalidAuthorizationHeader extends RuntimeException {
    public InvalidAuthorizationHeader(String message) {
        super(message);
    }
}
