package io.beanguard.server.controllers;

import io.beanguard.api.models.shop.CreateProductRequest;
import io.beanguard.api.models.shop.Product;
import io.beanguard.api.models.shop.UpdateProductRequest;
import io.beanguard.server.services.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductsController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable UUID id) {
        return productService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody @Valid CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id, @RequestBody @Valid UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }

    @PutMapping("/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderProducts(@RequestBody List<UUID> orderedIds) {
        productService.reorderProducts(orderedIds);
    }
}
