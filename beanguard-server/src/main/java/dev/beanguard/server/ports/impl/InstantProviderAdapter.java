package dev.beanguard.server.ports.impl;

import dev.beanguard.server.ports.InstantProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class InstantProviderAdapter implements InstantProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public Instant plusMonth() {
        return plusMonth(now());
    }

    @Override
    public Instant plusMonth(Instant currentInstant) {
        return plusMonths(1, currentInstant);
    }

    @Override
    public Instant plusMonths(int months, Instant currentInstant) {
        ZonedDateTime zonedDateTime = currentInstant.atZone(ZoneId.of("UTC"));
        ZonedDateTime nextMonth = zonedDateTime.plusMonths(months);
        return nextMonth.toInstant();
    }

    @Override
    public Instant plusDays(int days) {
        return now().plus(days, ChronoUnit.DAYS);
    }
}
