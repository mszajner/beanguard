package dev.beanguard.server.ports;

import java.time.Instant;

public interface InstantProvider {
    Instant now();

    Instant plusMonth();

    Instant plusMonth(Instant currentInstant);

    Instant plusMonths(int months, Instant currentInstant);

    Instant plusDays(int days);
}
