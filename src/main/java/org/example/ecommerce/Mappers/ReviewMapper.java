package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.AddReviewDto;
import org.example.ecommerce.DTO.ShowReviewDto;
import org.example.ecommerce.Model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    Review reviewDtoToReview(AddReviewDto addReviewDto);

    @Mapping(target = "username", source = "user.username")
    ShowReviewDto reviewToShowReviewDto(Review review);
}
