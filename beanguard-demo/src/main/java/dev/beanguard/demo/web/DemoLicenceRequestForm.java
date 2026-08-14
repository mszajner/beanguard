package dev.beanguard.demo.web;

import dev.beanguard.api.validators.PolishNIP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoLicenceRequestForm {

    @NotBlank(message = "{demo.validation.notBlank}")
    @Email(message = "{demo.validation.email}")
    private String email;

    @NotBlank(message = "{demo.validation.notBlank}")
    @PolishNIP(message = "{demo.validation.vatId}")
    private String vatId;
}
