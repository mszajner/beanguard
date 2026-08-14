package dev.beanguard.server.repositories;

import dev.beanguard.server.entities.UserEntity;
import dev.beanguard.server.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    default long countByRolesContaining(Role role) {
        return countByRoleNameContaining(role.name());
    }

    @Query(value = "SELECT COUNT(*) FROM users WHERE roles @> jsonb_build_array(CAST(:roleName AS text))", nativeQuery = true)
    long countByRoleNameContaining(@Param("roleName") String roleName);
}
