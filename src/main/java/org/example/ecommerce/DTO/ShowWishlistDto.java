package org.example.ecommerce.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowWishlistDto(Long wishlistId,
                              Long productId,
                              String productName,
                              BigDecimal price,
                              String description,
                              LocalDateTime addedAt) {
}
