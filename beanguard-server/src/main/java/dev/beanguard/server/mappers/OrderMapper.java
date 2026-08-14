package dev.beanguard.server.mappers;

import dev.beanguard.api.models.shop.Order;
import dev.beanguard.api.models.shop.OrderItem;
import dev.beanguard.server.entities.OrderEntity;
import dev.beanguard.server.entities.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    @Mapping(target = "proFormaNumber", ignore = true)
    @Mapping(target = "number", source = "number")
    Order toDto(OrderEntity entity);

    @Mapping(target = "productId", source = "product.id")
    OrderItem toOrderItemDto(OrderItemEntity entity);
}
