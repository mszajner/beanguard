package io.beanguard.server.mappers;

import io.beanguard.api.models.shop.Order;
import io.beanguard.api.models.shop.OrderItem;
import io.beanguard.server.entities.OrderEntity;
import io.beanguard.server.entities.OrderItemEntity;
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
