package io.beanguard.server.services;

import io.beanguard.server.models.KeyRotationResult;

public interface TokenKeyService {
    KeyRotationResult regenerateKeys();
}
