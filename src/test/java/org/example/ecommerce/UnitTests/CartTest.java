package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Model.Cart;
import org.example.ecommerce.Model.CartItem;
import org.example.ecommerce.Model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Cart rules tested through Cart's own interface: no mocks, no Spring. */
class CartTest {

    private static Product product(long id, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("Product " + id);
        product.setPrice(new BigDecimal(price));
        product.setAvailableQuantity(stock);
        return product;
    }

    @Test
    void add_newProduct_createsLineWithQuantityOne() {
        Cart cart = new Cart();

        CartItem item = cart.add(product(1, "10.00", 5));

        assertThat(cart.getCartItems()).containsExactly(item);
        assertThat(item.getQuantity()).isEqualTo(1);
        assertThat(item.getCart()).isSameAs(cart);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void add_sameProductTwice_mergesIntoOneLine() {
        Cart cart = new Cart();
        Product p = product(1, "10.00", 5);

        cart.add(p);
        cart.add(p);

        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void add_outOfStockProduct_throwsAndLeavesCartUnchanged() {
        Cart cart = new Cart();

        assertThrows(OutOfStockException.class, () -> cart.add(product(1, "10.00", 0)));

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void add_beyondStock_throws() {
        Cart cart = new Cart();
        Product p = product(1, "10.00", 1);
        cart.add(p);

        assertThrows(OutOfStockException.class, () -> cart.add(p));
        assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(1);
    }

    @Test
    void increment_inStock_increasesQuantityAndTotal() {
        Cart cart = new Cart();
        cart.add(product(1, "10.00", 5));

        cart.increment(1L);

        assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void increment_beyondStock_throws() {
        Cart cart = new Cart();
        cart.add(product(1, "10.00", 1));

        assertThrows(OutOfStockException.class, () -> cart.increment(1L));
        assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(1);
    }

    @Test
    void increment_productNotInCart_throws() {
        assertThrows(ProductNotFoundException.class, () -> new Cart().increment(1L));
    }

    @Test
    void decrement_aboveOne_decreases() {
        Cart cart = new Cart();
        Product p = product(1, "10.00", 5);
        cart.add(p);
        cart.add(p);

        cart.decrement(1L);

        assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(1);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void decrement_atOne_removesLine() {
        Cart cart = new Cart();
        cart.add(product(1, "10.00", 5));

        cart.decrement(1L);

        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("0");
    }

    @Test
    void decrement_productNotInCart_throws() {
        assertThrows(ProductNotFoundException.class, () -> new Cart().decrement(1L));
    }

    @Test
    void remove_dropsOnlyThatProduct() {
        Cart cart = new Cart();
        cart.add(product(1, "10.00", 5));
        cart.add(product(2, "3.50", 5));

        cart.remove(1L);

        assertThat(cart.getCartItems()).extracting(i -> i.getProduct().getId()).containsExactly(2L);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("3.50");
    }

    @Test
    void remove_productNotInCart_isNoOp() {
        Cart cart = new Cart();
        cart.add(product(1, "10.00", 5));

        cart.remove(99L);

        assertThat(cart.getCartItems()).hasSize(1);
    }

    @Test
    void clear_emptiesCartAndZeroesTotal() {
        Cart cart = new Cart();
        cart.add(product(1, "10.00", 5));

        cart.clear();

        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("0");
    }

    @Test
    void total_sumsAllLines() {
        Cart cart = new Cart();
        Product a = product(1, "10.00", 5);
        cart.add(a);
        cart.add(a);
        cart.add(product(2, "2.25", 5));

        assertThat(cart.total()).isEqualByComparingTo("22.25");
    }
}
