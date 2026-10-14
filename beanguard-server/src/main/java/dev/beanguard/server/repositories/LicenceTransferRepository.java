package dev.beanguard.server.repositories;

import dev.beanguard.server.entities.LicenceTransferEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LicenceTransferRepository extends JpaRepository<LicenceTransferEntity, UUID> {
    void deleteByLicenceKeyAndStatus(UUID licenceKey, String status);

    boolean existsByLicenceKeyAndStatusAndCreatedAtAfter(UUID licenceKey, String status, Instant createdAfter);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from LicenceTransferEntity t where t.token = :token")
    Optional<LicenceTransferEntity> findByTokenForUpdate(@Param("token") UUID token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from LicenceTransferEntity t where t.confirmToken = :confirmToken")
    Optional<LicenceTransferEntity> findByConfirmTokenForUpdate(@Param("confirmToken") UUID confirmToken);
}
