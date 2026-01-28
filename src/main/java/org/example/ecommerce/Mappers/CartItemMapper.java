package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.ShowCartItemDto;
import org.example.ecommerce.Model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "product.description", target = "description")
    @Mapping(source = "quantity", target = "quantity")
    ShowCartItemDto cartItemToShowCartItemDto(CartItem cartItem);

}
