package io.beanguard.server.repositories;

import io.beanguard.server.entities.OrderSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderSequenceRepository extends JpaRepository<OrderSequenceEntity, Integer> {

    @Modifying
    @Query(value = "INSERT INTO order_sequence (year, last_number) VALUES (:year, 0) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void initializeYear(@Param("year") int year);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM OrderSequenceEntity s WHERE s.year = :year")
    Optional<OrderSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
