package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    AdminCategoryDto categoryToAdminCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    Category categoryNameToCategory(String categoryName);

    String categoryToCategoryName(Category category);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateCategory(String categoryName, @MappingTarget Category category);
}
