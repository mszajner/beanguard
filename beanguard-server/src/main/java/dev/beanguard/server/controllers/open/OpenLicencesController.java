package dev.beanguard.server.controllers.open;

import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.api.models.shop.LicenceTokenResponse;
import dev.beanguard.server.exceptions.InvalidAuthorizationHeader;
import dev.beanguard.server.services.LicenceService;
import dev.beanguard.server.services.LicenceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/open/licences")
@RequiredArgsConstructor
@Tag(name = "Licences")
public class OpenLicencesController {

    private final static String KEY_SECRET_HEADER_PREFIX = "KeySecret ";
    private final static String KEY_HEADER_PREFIX = "Key ";

    private final LicenceService licenceService;
    private final LicenceTokenService licenceTokenService;

    @GetMapping
    @Operation(summary = "Get encrypted licence for specified key and secret.",
            description = "You should provide key and secret in Authorization header in format \"KeySecret base64(key:secret)\"")
    public String getLicence(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith(KEY_SECRET_HEADER_PREFIX)) {
                throw new IllegalArgumentException();
            }
            String base64Credentials = authorizationHeader.substring(KEY_SECRET_HEADER_PREFIX.length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String decodedCredentials = new String(decodedBytes);
            String[] parts = decodedCredentials.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }
            String key = parts[0];
            String secret = parts[1];
            return licenceService.getLicence(UUID.fromString(key), secret);
        } catch (IllegalArgumentException e) {
            throw new InvalidAuthorizationHeader("Invalid authorization header format");
        }
    }

    @GetMapping("/iat")
    @Operation(summary = "Return issuedAt of licence for specified key",
            description = "You should provide key in Authorization header in format \"Key base64(key)\"")
    public String getLicenceIat(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith(KEY_HEADER_PREFIX)) {
                throw new IllegalArgumentException();
            }
            String base64Credentials = authorizationHeader.substring(KEY_HEADER_PREFIX.length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String decodedCredentials = new String(decodedBytes);
            return licenceService.getLicenceIat(UUID.fromString(decodedCredentials)).toString();
        } catch (IllegalArgumentException e) {
            throw new InvalidAuthorizationHeader("Invalid authorization header format");
        }
    }

    @PostMapping
    @Operation(summary = "Generates new licence for specified e-mail and vatId.")
    public String createLicence(@RequestBody @Valid LicenceDemoCreateRequest licence) {
        return licenceService.createDemoLicence(licence);
    }

    @PostMapping("/token")
    @Operation(summary = "Generate shop redirect token for an authenticated licence.",
            description = "Provide key and secret in Authorization header: \"KeySecret base64(key:secret)\"")
    public LicenceTokenResponse generateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith(KEY_SECRET_HEADER_PREFIX)) {
                throw new IllegalArgumentException();
            }
            String base64Credentials = authorizationHeader.substring(KEY_SECRET_HEADER_PREFIX.length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String[] parts = new String(decodedBytes).split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }
            UUID key = UUID.fromString(parts[0]);
            String secret = parts[1];
            licenceService.getLicence(key, secret);
            return licenceTokenService.generateToken(key);
        } catch (IllegalArgumentException e) {
            throw new InvalidAuthorizationHeader("Invalid authorization header format");
        }
    }
}
