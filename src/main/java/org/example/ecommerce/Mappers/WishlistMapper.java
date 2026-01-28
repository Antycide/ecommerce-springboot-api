package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.ShowWishlistDto;
import org.example.ecommerce.DTO.WishlistResponseDto;
import org.example.ecommerce.Model.Wishlist;
import org.example.ecommerce.Model.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(source = "wishlist.id", target = "wishlistId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "product.description", target = "description")
    ShowWishlistDto wishlistItemToShowWishlistDto(WishlistItem wishlistItem);

    @Mapping(source = "wishlistItems", target = "wishlist")
    WishlistResponseDto wishlistToWishlistResponseDto(Wishlist wishlist);
}
