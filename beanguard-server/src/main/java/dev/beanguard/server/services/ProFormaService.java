package dev.beanguard.server.services;

import dev.beanguard.server.entities.OrderEntity;
import dev.beanguard.server.entities.ProFormaEntity;

import java.time.LocalDate;
import java.util.UUID;

public interface ProFormaService {
    ProFormaEntity generate(OrderEntity order, LocalDate issueDate, UUID licenceKey, LocalDate projectedExpiry);
}
