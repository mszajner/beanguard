package io.beanguard.server.services;

import io.beanguard.server.entities.OrderEntity;
import io.beanguard.server.entities.ProFormaEntity;

import java.time.LocalDate;
import java.util.UUID;

public interface ProFormaService {
    ProFormaEntity generate(OrderEntity order, LocalDate issueDate, UUID licenceKey, LocalDate projectedExpiry);
}
