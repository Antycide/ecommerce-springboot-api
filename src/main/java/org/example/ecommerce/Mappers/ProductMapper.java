package org.example.ecommerce.Mappers;


import org.example.ecommerce.DTO.AddProductDto;
import org.example.ecommerce.DTO.ShowAdminProductDto;
import org.example.ecommerce.DTO.ShowProductDto;
import org.example.ecommerce.Model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wishlistItems", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Product addProductDtoToProduct(AddProductDto addProductDto);

    @Mapping(source = "category", target = "categoryName")
    AddProductDto productToAddProductDto(Product product);

    ShowProductDto productToShowProductDto(Product product);

    ShowAdminProductDto productToShowAdminProductDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "wishlistItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void updateProductFromDto(AddProductDto addProductDto, @MappingTarget Product product);
}
