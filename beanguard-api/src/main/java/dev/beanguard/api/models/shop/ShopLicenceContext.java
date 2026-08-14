package dev.beanguard.api.models.shop;

import dev.beanguard.api.models.licence.LicenceType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@Data
public class ShopLicenceContext {
    UUID licenceId;
    String companyName;
    String vatId;
    String email;
    String street;
    String postCode;
    String city;
    String phoneNumber;
    Instant expiration;
    Map<String, Integer> limitClaims;
    Set<String> featureClaims;
    BigDecimal licenceNetAmount;
    OrderPeriod licenceLastPeriod;
    LicenceType licenceType;
}
