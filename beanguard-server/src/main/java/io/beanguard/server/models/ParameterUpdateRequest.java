package io.beanguard.server.models;

import jakarta.validation.constraints.NotBlank;

public record ParameterUpdateRequest(@NotBlank String value) {}
