package io.beanguard.server.models;

import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder(toBuilder = true)
public record User(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Set<Role> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
