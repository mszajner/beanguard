package io.beanguard.api.models.licence;

import io.beanguard.api.validators.PolishNIP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenceDemoCreateRequest {
    @NotBlank
    @Email
    String email;
    @NotBlank
    @PolishNIP
    String vatId;
}
