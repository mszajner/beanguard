package io.beanguard.api.models.shop;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull ProductType type,
        @NotBlank String claim,
        @NotNull @DecimalMin("0.01") BigDecimal priceOneMonth,
        @NotNull @DecimalMin("0.01") BigDecimal priceOneYear,
        boolean enabled,
        boolean required,
        String certificateName) {}
