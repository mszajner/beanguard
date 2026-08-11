package io.beanguard.api.models.licence;

import io.beanguard.api.validators.PolishNIP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenceUpdateRequest {
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
}
