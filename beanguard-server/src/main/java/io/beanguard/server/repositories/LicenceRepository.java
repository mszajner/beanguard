package io.beanguard.server.repositories;

import io.beanguard.api.models.licence.LicenceType;
import io.beanguard.server.entities.LicenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenceRepository
        extends JpaRepository<LicenceEntity, String>,
                JpaSpecificationExecutor<LicenceEntity> {

    Optional<LicenceEntity> findByKeyAndSecret(UUID key, String secret);

    Optional<LicenceEntity> findByKey(UUID key);

    boolean existsByEmailAndType(String email, LicenceType type);

    List<LicenceEntity> findByExpirationBetween(Instant from, Instant to);
}
