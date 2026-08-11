package io.beanguard.server.controllers;

import io.beanguard.api.models.shop.Order;
import io.beanguard.server.services.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrdersController {

    private final OrderService orderService;

    @GetMapping
    public Page<Order> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getOrders(status, query, pageable);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/{id}/accept")
    public Order acceptOrder(@PathVariable UUID id) {
        return orderService.acceptOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public Order cancelOrder(@PathVariable UUID id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/{id}/pro-forma")
    public ResponseEntity<byte[]> getProFormaPdf(@PathVariable UUID id) {
        byte[] pdf = orderService.getProFormaPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"faktura-proforma.pdf\"")
                .body(pdf);
    }
}
