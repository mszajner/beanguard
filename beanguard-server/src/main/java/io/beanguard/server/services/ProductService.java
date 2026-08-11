package io.beanguard.server.services;

import io.beanguard.api.models.shop.CreateProductRequest;
import io.beanguard.api.models.shop.Product;
import io.beanguard.api.models.shop.UpdateProductRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> getProducts();
    List<Product> getEnabledProducts();
    Product getProduct(UUID id);
    Product createProduct(CreateProductRequest request);
    Product updateProduct(UUID id, UpdateProductRequest request);
    void deleteProduct(UUID id);
    void reorderProducts(List<UUID> orderedIds);
}
