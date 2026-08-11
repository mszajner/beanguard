package io.beanguard.server.repositories;

import io.beanguard.server.entities.ProFormaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProFormaRepository extends JpaRepository<ProFormaEntity, UUID> {

    Optional<ProFormaEntity> findByOrder_Id(UUID orderId);

    List<ProFormaEntity> findByOrder_IdIn(Collection<UUID> orderIds);
}
