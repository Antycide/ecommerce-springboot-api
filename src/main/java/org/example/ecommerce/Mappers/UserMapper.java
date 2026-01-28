package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.RegisteredUserDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // password will be hashed in the service class
    @Mapping(target = "wishlist", ignore = true)
    @Mapping(target = "firstname", ignore = true)
    @Mapping(target = "lastname", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "role", expression = "java(org.example.ecommerce.Model.UserRole.CUSTOMER)")
    @Mapping(target = "authorities", ignore = true)
    User toUser(UserRegistrationDto userRegistrationDto);

    UserRegistrationDto toUserRegistrationDto(User user);

    RegisteredUserDto userToRegisteredUserDto(User user);

}
