package io.beanguard.server.repositories;

import io.beanguard.server.entities.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {
}
