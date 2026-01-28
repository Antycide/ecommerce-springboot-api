package org.example.ecommerce.Mappers;

import org.example.ecommerce.Model.CartItem;
import org.example.ecommerce.Model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(source = "cartItem.quantity", target = "quantity")
    @Mapping(source = "cartItem.product", target = "product")
    @Mapping(target = "subtotal", source = "subtotal")
    OrderItem cartItemToOrderItem(CartItem cartItem);
}
