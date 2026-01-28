package org.example.ecommerce.DTO;

import jakarta.validation.constraints.NotNull;
import org.example.ecommerce.Model.ShippingType;

public record CreateOrderRequest(@NotNull  Long addressId,
                                @NotNull ShippingType shippingType) {
}
