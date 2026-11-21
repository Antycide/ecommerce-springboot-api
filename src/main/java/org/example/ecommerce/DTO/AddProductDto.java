package org.example.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AddProductDto(@NotBlank(message = "Product name cannot be empty") String productName,
                            @NotBlank(message = "description cannot be empty") String description,
                            @NotNull(message = "price cannot be empty") @Positive(message = "price must be greater than 0") BigDecimal price,
                            @NotNull(message = "Available quantity cannot be empty") @PositiveOrZero(message = "Available quantity cannot be negative") Integer availableQuantity,
                            @NotBlank(message = "Category name cannot be empty") String categoryName) {
}
