package dev.beanguard.server.models;

import java.time.Instant;

public record KeyRotationResult(String publicKey, String secretKey, Instant rotatedAt) {}
