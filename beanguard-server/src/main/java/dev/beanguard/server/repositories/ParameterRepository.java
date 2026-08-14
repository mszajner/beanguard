package dev.beanguard.server.repositories;

import dev.beanguard.server.entities.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {
}
