package org.example.ecommerce.DTO;

import java.util.List;

public record JwtResponseDto(String jwtToken,
                             String username,
                             List<String> roles) {
}
