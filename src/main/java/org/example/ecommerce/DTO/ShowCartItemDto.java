package org.example.ecommerce.DTO;

public record ShowCartItemDto(Long productId,
                              String productName,
                              String description,
                              double price,
                              int quantity) {
}
