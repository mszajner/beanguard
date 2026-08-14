package dev.beanguard.server.services;

import dev.beanguard.api.models.shop.CreateOrderRequest;
import dev.beanguard.api.models.shop.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);
    Order getOrder(UUID id);
    Page<Order> getOrders(String status, String query, Pageable pageable);
    Order acceptOrder(UUID id);
    Order cancelOrder(UUID id);
    byte[] getProFormaPdf(UUID orderId);
}
