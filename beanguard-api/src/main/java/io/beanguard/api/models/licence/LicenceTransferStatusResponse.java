package io.beanguard.api.models.licence;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LicenceTransferStatusResponse(String status, String secret) {}
