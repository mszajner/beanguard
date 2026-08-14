package dev.beanguard.api.models.shop;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull OrderPeriod period,
        UUID licenceId,
        @NotBlank String vatId,
        @NotBlank @Email String email,
        @NotBlank String companyName,
        @NotBlank String street,
        @NotBlank String postCode,
        @NotBlank String city,
        @NotBlank String phoneNumber,
        @NotEmpty List<CreateOrderItemRequest> items) {}
