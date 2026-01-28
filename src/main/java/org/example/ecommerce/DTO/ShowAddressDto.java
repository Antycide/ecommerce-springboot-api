package org.example.ecommerce.DTO;

public record ShowAddressDto(Long id,
                             String streetAddress,
                             String city,
                             String state,
                             String postalCode,
                             String country,
                             String addressType,
                             boolean isDefault) {
}
