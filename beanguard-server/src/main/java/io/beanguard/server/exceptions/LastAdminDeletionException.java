package io.beanguard.server.exceptions;

public class LastAdminDeletionException extends RuntimeException {
    public LastAdminDeletionException() {
        super("Cannot delete the last administrator");
    }
}
