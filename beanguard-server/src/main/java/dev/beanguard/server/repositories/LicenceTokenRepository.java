package dev.beanguard.server.repositories;

import dev.beanguard.server.entities.LicenceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface LicenceTokenRepository extends JpaRepository<LicenceTokenEntity, UUID> {

    void deleteByExpiresAtBefore(Instant threshold);
}
