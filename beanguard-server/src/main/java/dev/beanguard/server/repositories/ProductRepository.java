package dev.beanguard.server.repositories;

import dev.beanguard.server.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findAllByOrderBySortOrderAsc();
    List<ProductEntity> findAllByEnabledTrueOrderBySortOrderAsc();
}
