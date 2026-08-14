package dev.beanguard.server.repositories;

import dev.beanguard.server.entities.LicenceTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenceTransferRepository extends JpaRepository<LicenceTransferEntity, UUID> {
    void deleteByLicenceKeyAndStatus(UUID licenceKey, String status);
}
