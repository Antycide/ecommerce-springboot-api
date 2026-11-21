package org.example.ecommerce.DTO;

import java.math.BigDecimal;

public record ShowAdminProductDto(Long id,
                                  String productName,
                                  String description,
                                  BigDecimal price,
                                  Integer availableQuantity
                           ) {
}
