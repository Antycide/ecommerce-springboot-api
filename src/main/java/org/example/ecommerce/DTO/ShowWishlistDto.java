package org.example.ecommerce.DTO;

import java.time.LocalDateTime;

public record ShowWishlistDto(Long wishlistId,
                              String productName,
                              Double price,
                              String description,
                              LocalDateTime addedAt) {
}
