package io.beanguard.api.models.shop;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Product(UUID id, boolean enabled, ProductType type, String claim,
                      BigDecimal priceOneMonth, BigDecimal priceOneYear,
                      String name, String description, String certificateName,
                      Instant createdAt, Instant updatedAt, int sortOrder, boolean required) {}
