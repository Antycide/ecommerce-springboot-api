package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {




    ShowOrderDto orderToShowOrderDto(Order order);
    ShowCheckoutOrderDto orderToShowCheckoutOrderDto(Order order);
}
