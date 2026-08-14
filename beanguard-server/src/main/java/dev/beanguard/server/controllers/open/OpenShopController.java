package dev.beanguard.server.controllers.open;

import dev.beanguard.api.models.shop.CreateOrderRequest;
import dev.beanguard.api.models.shop.Order;
import dev.beanguard.api.models.shop.Product;
import dev.beanguard.api.models.shop.ShopLicenceContext;
import dev.beanguard.server.services.LicenceService;
import dev.beanguard.server.services.OrderService;
import dev.beanguard.server.services.ProductService;
import dev.beanguard.server.services.LicenceTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/open/shop")
@RequiredArgsConstructor
@Tag(name = "Shop")
public class OpenShopController {

    private final LicenceTokenService licenceTokenService;
    private final LicenceService licenceService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/licence")
    public ShopLicenceContext getLicenceContext(@RequestParam UUID token) {
        UUID licenceKey = licenceTokenService.resolveToken(token);
        return licenceService.getShopContext(licenceKey);
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productService.getEnabledProducts();
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return orderService.createOrder(request);
    }
}
