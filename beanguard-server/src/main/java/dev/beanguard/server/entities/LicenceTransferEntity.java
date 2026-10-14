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
@Table(name = "licence_transfers")
public class LicenceTransferEntity {

    /** Returned to whoever initiated the transfer; it reads the outcome but cannot confirm. */
    @Id
    private UUID token;

    @Column(name = "licence_key", nullable = false)
    private UUID licenceKey;

    /** Emailed to the licence owner only; it confirms the transfer but cannot read its outcome. */
    @Column(name = "confirm_token", nullable = false, unique = true)
    private UUID confirmToken;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
