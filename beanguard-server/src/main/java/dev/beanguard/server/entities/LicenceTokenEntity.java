package dev.beanguard.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "licence_tokens")
public class LicenceTokenEntity {

    @Id
    private UUID token;

    @Column(name = "licence_key", nullable = false)
    private UUID licenceKey;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
