package io.beanguard.server.controllers;

import io.beanguard.server.models.KeyRotationResult;
import io.beanguard.server.services.TokenKeyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token-keys")
@RequiredArgsConstructor
@Tag(name = "Token Keys")
public class TokenKeysController {

    private final TokenKeyService tokenKeyService;

    @PostMapping("/regenerate")
    public KeyRotationResult regenerate() {
        return tokenKeyService.regenerateKeys();
    }
}
