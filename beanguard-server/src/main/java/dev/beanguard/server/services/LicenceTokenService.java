package dev.beanguard.server.services;

import dev.beanguard.api.models.shop.LicenceTokenResponse;

import java.util.UUID;

public interface LicenceTokenService {
    LicenceTokenResponse generateToken(UUID licenceKey);
    UUID resolveToken(UUID token);
}
