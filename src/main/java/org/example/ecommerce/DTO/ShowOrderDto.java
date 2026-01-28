package org.example.ecommerce.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowOrderDto(Long id,
                           LocalDateTime orderDate,
                           String orderStatus,
                           String orderNumber,
                           BigDecimal totalCostAmount,
                           String shippingType) {
}
