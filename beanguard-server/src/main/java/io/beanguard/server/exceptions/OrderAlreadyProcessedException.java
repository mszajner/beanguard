package io.beanguard.server.exceptions;

import java.util.UUID;

public class OrderAlreadyProcessedException extends RuntimeException {
    public OrderAlreadyProcessedException(UUID id) {
        super("Order " + id + " has already been processed");
    }
}
