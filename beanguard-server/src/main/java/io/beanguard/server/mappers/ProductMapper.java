package io.beanguard.server.mappers;

import io.beanguard.api.models.shop.CreateProductRequest;
import io.beanguard.api.models.shop.Product;
import io.beanguard.server.entities.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toDto(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductEntity toEntity(CreateProductRequest request);
}
