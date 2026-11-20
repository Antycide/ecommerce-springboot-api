package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Model.OrderItem;
import org.example.ecommerce.Model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    void subtotal_setAtPlacement_isNotRecomputedFromLaterPrice() {
        Product product = new Product();
        product.setPrice(new BigDecimal("10.00"));
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("20.00"));

        product.setPrice(new BigDecimal("99.00"));
        item.calculateSubtotal();

        assertThat(item.getSubtotal()).isEqualByComparingTo("20.00");
    }

    @Test
    void subtotal_missing_isFilledFromPrice() {
        Product product = new Product();
        product.setPrice(new BigDecimal("2.50"));
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(3);

        item.calculateSubtotal();

        assertThat(item.getSubtotal()).isEqualByComparingTo("7.50");
    }
}
