package io.beanguard.server.repositories;

import io.beanguard.server.entities.LicenceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface LicenceTokenRepository extends JpaRepository<LicenceTokenEntity, UUID> {

    void deleteByExpiresAtBefore(Instant threshold);
}
