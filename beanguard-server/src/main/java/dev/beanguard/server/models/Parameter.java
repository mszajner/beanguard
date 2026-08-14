package dev.beanguard.server.models;

import java.time.Instant;

public record Parameter(String name, String value, Instant createdAt, Instant updatedAt) {}
