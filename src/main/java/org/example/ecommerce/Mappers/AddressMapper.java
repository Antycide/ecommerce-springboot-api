package org.example.ecommerce.Mappers;

import org.example.ecommerce.DTO.AddAddressDto;
import org.example.ecommerce.DTO.ShowAddressDto;
import org.example.ecommerce.Model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "addressType", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    Address addAddressDtoToAddress(AddAddressDto address);


    @Mapping(source = "addressType", target = "addressType")
    ShowAddressDto addressToShowAddressDto(Address address);
}
