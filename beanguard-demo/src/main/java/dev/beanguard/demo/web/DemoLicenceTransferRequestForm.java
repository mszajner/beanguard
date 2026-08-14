package dev.beanguard.demo.web;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DemoLicenceTransferRequestForm {

    @NotNull(message = "{demo.validation.licenceKey}")
    private UUID licenceKey;
}
