package io.beanguard.server.exceptions;

public class DemoLicenceAlreadyExistsException extends RuntimeException {
    public DemoLicenceAlreadyExistsException(String email) {
        super("Demo licence for email " + email + " already exists");
    }
}
