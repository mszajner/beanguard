package io.beanguard.server.repositories;

import io.beanguard.server.entities.ProFormaSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProFormaSequenceRepository extends JpaRepository<ProFormaSequenceEntity, Integer> {

    @Modifying
    @Query(value = "INSERT INTO pro_forma_sequence (year, last_number) VALUES (:year, 0) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void initializeYear(@Param("year") int year);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProFormaSequenceEntity s WHERE s.year = :year")
    Optional<ProFormaSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
