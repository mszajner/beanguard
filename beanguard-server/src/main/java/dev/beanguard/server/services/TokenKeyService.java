package dev.beanguard.server.services;

import dev.beanguard.server.models.KeyRotationResult;

public interface TokenKeyService {
    KeyRotationResult regenerateKeys();
}
