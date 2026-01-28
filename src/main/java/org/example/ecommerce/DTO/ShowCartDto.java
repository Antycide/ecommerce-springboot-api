package org.example.ecommerce.DTO;

import java.util.List;

public record ShowCartDto(List<ShowCartItemDto> cart) {
}
