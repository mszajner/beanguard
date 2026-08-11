package io.beanguard.api.models.licence;

import io.beanguard.api.models.licence.LicenceType;
import io.beanguard.api.validators.PolishNIP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

import java.time.Instant;
import java.util.Map;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenceCreateRequest {
    Instant expiration;
    String companyName;
    String street;
    String postCode;
    String city;
    @NotBlank
    @PolishNIP
    String vatId;
    @NotBlank
    @Email
    String email;
    String phoneNumber;
    Map<String, String> claims;
    @Default
    LicenceType type = LicenceType.STANDARD;
}
