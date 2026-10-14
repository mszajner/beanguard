package dev.beanguard.server.controllers.open;

import dev.beanguard.api.models.licence.LicenceTransferInitRequest;
import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;
import dev.beanguard.server.services.LicenceTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/open/licences/transfer")
@RequiredArgsConstructor
@Tag(name = "Licences")
public class OpenTransferController {

    private final LicenceTransferService licenceTransferService;

    @PostMapping
    @Operation(summary = "Initiate machine transfer for a licence key. The owner is emailed a confirmation link; "
            + "the response is identical whether or not the key is eligible.")
    public LicenceTransferInitResponse initiateTransfer(@RequestBody @Valid LicenceTransferInitRequest request) {
        return licenceTransferService.initiateTransfer(request.licenceKey());
    }

    @GetMapping("/{token}")
    @Operation(summary = "Poll transfer status. Returns PENDING, EXPIRED, or CONFIRMED with the new secret "
            + "(rotated on this call; it can be collected once).")
    public LicenceTransferStatusResponse getStatus(@PathVariable UUID token) {
        return licenceTransferService.getStatus(token);
    }

    @PostMapping("/{token}/confirm")
    @Operation(summary = "Confirm transfer from the emailed link (shop frontend). Takes the token from the email, "
            + "not the one returned by the initiate call.")
    public Map<String, String> confirm(@PathVariable UUID token) {
        licenceTransferService.confirm(token);
        return Map.of("status", "CONFIRMED");
    }
}
