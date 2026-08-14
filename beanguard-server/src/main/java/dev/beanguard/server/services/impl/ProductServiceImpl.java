package dev.beanguard.server.services.impl;

import dev.beanguard.api.models.shop.CreateProductRequest;
import dev.beanguard.api.models.shop.Product;
import dev.beanguard.api.models.shop.UpdateProductRequest;
import dev.beanguard.server.entities.ProductEntity;
import dev.beanguard.server.exceptions.ProductNotFoundException;
import dev.beanguard.server.mappers.ProductMapper;
import dev.beanguard.server.repositories.ProductRepository;
import dev.beanguard.server.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<Product> getProducts() {
        return productRepository.findAllByOrderBySortOrderAsc().stream().map(productMapper::toDto).toList();
    }

    @Override
    public List<Product> getEnabledProducts() {
        return productRepository.findAllByEnabledTrueOrderBySortOrderAsc().stream().map(productMapper::toDto).toList();
    }

    @Override
    public Product getProduct(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        ProductEntity entity = productMapper.toEntity(request);
        int maxOrder = productRepository.findAllByOrderBySortOrderAsc().stream()
                .mapToInt(ProductEntity::getSortOrder).max().orElse(-1);
        entity.setSortOrder(maxOrder + 1);
        return productMapper.toDto(productRepository.save(entity));
    }

    @Override
    @Transactional
    public Product updateProduct(UUID id, UpdateProductRequest request) {
        var entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setClaim(request.claim());
        entity.setPriceOneMonth(request.priceOneMonth());
        entity.setPriceOneYear(request.priceOneYear());
        entity.setEnabled(request.enabled());
        entity.setRequired(request.required());
        entity.setCertificateName(request.certificateName());
        return productMapper.toDto(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void reorderProducts(List<UUID> orderedIds) {
        Map<UUID, ProductEntity> entityMap = productRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(ProductEntity::getId, e -> e));
        IntStream.range(0, orderedIds.size()).forEach(i -> {
            ProductEntity entity = entityMap.get(orderedIds.get(i));
            if (entity != null) {
                entity.setSortOrder(i);
            }
        });
        productRepository.saveAll(entityMap.values());
    }
}
