package io.beanguard.server.services;

import io.beanguard.api.models.shop.LicenceTokenResponse;

import java.util.UUID;

public interface LicenceTokenService {
    LicenceTokenResponse generateToken(UUID licenceKey);
    UUID resolveToken(UUID token);
}
