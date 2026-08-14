package dev.beanguard.api.models.licence;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
@Getter
@Setter
@Data
public class Licence {
    UUID key;
    String secret;
    Instant expiration;
    String companyName;
    String street;
    String postCode;
    String city;
    String vatId;
    String email;
    String phoneNumber;
    Map<String, String> claims;
    LicenceType type;
    BigDecimal netAmount;
    Instant createdAt;
    Instant updatedAt;
}
