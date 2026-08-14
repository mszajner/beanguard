package dev.beanguard.server.repositories;

import dev.beanguard.api.models.shop.OrderStatus;
import dev.beanguard.server.entities.OrderEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>,
        JpaSpecificationExecutor<OrderEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
    Optional<OrderEntity> findByIdForUpdate(UUID id);

    List<OrderEntity> findByLicenceIdAndStatusNotIn(UUID licenceId, List<OrderStatus> statuses);
}
