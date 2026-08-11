package io.beanguard.server.controllers;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.api.models.licence.LicenceCreateRequest;
import io.beanguard.api.models.licence.LicenceType;
import io.beanguard.api.models.licence.LicenceUpdateRequest;
import io.beanguard.api.models.shop.LicenceTokenResponse;
import io.beanguard.server.services.LicenceService;
import io.beanguard.server.services.LicenceTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/licences")
@RequiredArgsConstructor
@Tag(name = "Licences")
public class LicencesController {

    private final LicenceService licenceService;
    private final LicenceTokenService licenceTokenService;

    @GetMapping
    public Page<Licence> getLicences(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) LicenceType type,
            @RequestParam(required = false) String expirationStatus) {
        return licenceService.getLicences(query, type, expirationStatus, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Licence createLicence(@RequestBody @Valid LicenceCreateRequest licence) {
        return licenceService.createLicence(licence);
    }

    @GetMapping("/{key}")
    public Licence getLicence(@PathVariable UUID key) {
        return licenceService.getLicence(key);
    }

    @PutMapping("/{key}")
    public Licence updateLicence(@PathVariable UUID key, @RequestBody @Valid LicenceUpdateRequest licence) {
        return licenceService.updateLicence(key, licence);
    }

    @PostMapping("/{key}/token")
    public LicenceTokenResponse generateShopToken(@PathVariable UUID key) {
        return licenceTokenService.generateToken(key);
    }

    @GetMapping("/{key}/certificate")
    public ResponseEntity<byte[]> getCertificate(@PathVariable UUID key) {
        byte[] pdf = licenceService.getCertificate(key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"certyfikat-" + key + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
