package org.example.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddProductDto(@NotBlank(message = "Product name cannot be empty") String productName,
                            @NotBlank(message = "description cannot be empty") String description,
                            @NotNull(message = "price cannot be empty") Double price,
                            @NotNull(message = "Available quantity cannot be empty") Integer availableQuantity,
                            @NotBlank(message = "Category name cannot be empty") String categoryName) {
}
