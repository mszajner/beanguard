package io.beanguard.api.models.shop;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(UUID id, OrderStatus status, UUID licenceId, OrderPeriod period,
                    String companyName, String street, String postCode, String city,
                    String vatId, String email, String phoneNumber,
                    List<OrderItem> items, BigDecimal creditAmount, Instant createdAt, Instant updatedAt,
                    String number, String proFormaNumber) {}
