package dev.beanguard.demo.web;

import dev.beanguard.api.validators.PolishNIP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoLicenceRequestForm {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @PolishNIP
    private String vatId;
}
