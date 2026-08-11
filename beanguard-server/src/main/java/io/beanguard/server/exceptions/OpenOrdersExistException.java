package io.beanguard.server.exceptions;

import java.util.List;

public class OpenOrdersExistException extends RuntimeException {

    private final List<String> openOrderIds;

    public OpenOrdersExistException(List<String> openOrderIds) {
        super("Dla tej licencji istnieją już otwarte zamówienia: " + String.join(", ", openOrderIds));
        this.openOrderIds = openOrderIds;
    }

    public List<String> getOpenOrderIds() {
        return openOrderIds;
    }
}
