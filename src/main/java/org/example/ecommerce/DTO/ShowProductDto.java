package org.example.ecommerce.DTO;

import java.math.BigDecimal;

public record ShowProductDto(Long id,
                             String productName,
                             String description,
                             BigDecimal price) {
}
