package io.beanguard.server.repositories;

import io.beanguard.server.entities.LicenceTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenceTransferRepository extends JpaRepository<LicenceTransferEntity, UUID> {
    void deleteByLicenceKeyAndStatus(UUID licenceKey, String status);
}
