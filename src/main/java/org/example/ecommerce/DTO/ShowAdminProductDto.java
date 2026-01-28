package org.example.ecommerce.DTO;

public record ShowAdminProductDto(Long id,
                                  String productName,
                                  String description,
                                  Double price,
                                  Integer availableQuantity
                           ) {
}
