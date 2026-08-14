package dev.beanguard.demo.web;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoServerConfigForm {

    @NotBlank(message = "{demo.validation.notBlank}")
    private String url;

    @NotBlank(message = "{demo.validation.notBlank}")
    private String publicKey;

    @NotBlank(message = "{demo.validation.notBlank}")
    private String secretKey;
}
