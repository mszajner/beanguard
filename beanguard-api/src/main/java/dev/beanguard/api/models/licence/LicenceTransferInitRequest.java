package dev.beanguard.api.models.licence;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LicenceTransferInitRequest(@NotNull UUID licenceKey) {}
