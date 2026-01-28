package org.example.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;

public record AddAddressDto(@NotBlank(message = "Street address cannot be empty") String streetAddress,
                            @NotBlank(message = "City cannot be empty") String city,
                            @NotBlank(message = "State cannot be empty") String state,
                            @NotBlank(message = "Postal code cannot be empty") String postalCode,
                            @NotBlank(message = "Country cannot be empty") String country) {
}
