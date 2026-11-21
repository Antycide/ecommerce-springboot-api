package org.example.ecommerce.DTO;

import java.math.BigDecimal;

public record ShowCartItemDto(Long productId,
                              String productName,
                              String description,
                              BigDecimal price,
                              int quantity) {
}
