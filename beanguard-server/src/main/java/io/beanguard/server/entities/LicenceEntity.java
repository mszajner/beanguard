package io.beanguard.server.entities;

import io.beanguard.api.models.licence.LicenceType;
import io.beanguard.api.models.shop.OrderPeriod;
import io.beanguard.server.mappers.JsonToMapConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "licences")
public class LicenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID key;

    @Column(name = "secret", nullable = false)
    private String secret;

    @Column(name = "expiration", nullable = false)
    private Instant expiration;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "street")
    private String street;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "city")
    private String city;

    @Column(name = "vat_id", nullable = false)
    private String vatId;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "claims")
    @Convert(converter = JsonToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> claims;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LicenceType type;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_period")
    private OrderPeriod lastPeriod;

    @Column(name = "certificate")
    private byte[] certificate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
