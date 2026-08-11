package io.beanguard.server.services.impl;

import io.beanguard.api.models.shop.LicenceTokenResponse;
import io.beanguard.server.entities.LicenceTokenEntity;
import io.beanguard.server.exceptions.InvalidLicenceTokenException;
import io.beanguard.server.repositories.LicenceTokenRepository;
import io.beanguard.server.services.LicenceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenceTokenServiceImpl implements LicenceTokenService {

    private static final int TOKEN_TTL_MINUTES = 15;

    private final LicenceTokenRepository licenceTokenRepository;

    @Value("${beanguard.shop.url}")
    private String shopBaseUrl;

    @Override
    @Transactional
    public LicenceTokenResponse generateToken(UUID licenceKey) {
        Instant now = Instant.now();
        licenceTokenRepository.deleteByExpiresAtBefore(now);
        Instant expiresAt = now.plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES);
        UUID token = UUID.randomUUID();
        licenceTokenRepository.save(new LicenceTokenEntity(token, licenceKey, expiresAt));
        return LicenceTokenResponse.builder()
                .shopUrl(shopBaseUrl + "/?token=" + token)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public UUID resolveToken(UUID token) {
        LicenceTokenEntity entity = licenceTokenRepository.findById(token)
                .orElseThrow(() -> new InvalidLicenceTokenException(token));
        if (entity.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidLicenceTokenException(token);
        }
        return entity.getLicenceKey();
    }
}
