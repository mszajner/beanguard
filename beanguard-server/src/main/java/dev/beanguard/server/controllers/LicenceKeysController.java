package dev.beanguard.server.controllers;

import dev.beanguard.server.models.KeyRotationResult;
import dev.beanguard.server.services.LicenceKeyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/licence-keys")
@RequiredArgsConstructor
@Tag(name = "Licence Keys")
public class LicenceKeysController {

    private final LicenceKeyService licenceKeyService;

    @PostMapping("/regenerate")
    public KeyRotationResult regenerate() {
        return licenceKeyService.regenerateKeys();
    }
}
