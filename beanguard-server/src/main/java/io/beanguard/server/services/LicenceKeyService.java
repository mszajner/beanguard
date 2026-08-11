package io.beanguard.server.services;

import io.beanguard.server.models.KeyRotationResult;

public interface LicenceKeyService {
    KeyRotationResult regenerateKeys();
}
