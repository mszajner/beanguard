package io.beanguard.server.exceptions;

import java.util.UUID;

public class ProFormaNotFoundException extends RuntimeException {
    public ProFormaNotFoundException(UUID orderId) {
        super("No pro-forma found for order " + orderId);
    }
}
