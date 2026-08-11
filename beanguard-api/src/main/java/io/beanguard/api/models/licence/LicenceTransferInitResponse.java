package io.beanguard.api.models.licence;

import java.time.Instant;
import java.util.UUID;

public record LicenceTransferInitResponse(UUID transferToken, Instant expiresAt) {}
