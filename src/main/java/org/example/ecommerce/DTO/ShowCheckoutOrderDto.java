package org.example.ecommerce.DTO;

import java.time.LocalDateTime;

public record ShowCheckoutOrderDto(String orderNumber,
                                   String orderStatus,
                                   LocalDateTime orderDate,
                                   String shippingType,
                                   Double totalCostAmount) {

}
