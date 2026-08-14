package dev.beanguard.api.models.shop;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(UUID id, UUID productId, String name, BigDecimal price, int quantity) {}
