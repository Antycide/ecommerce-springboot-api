package org.example.ecommerce.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Exception.ProductNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO; // price without shipping; cached copy of total()

    /** Adds one unit of the product, merging with an existing line. Never exceeds available stock. */
    public CartItem add(Product product) {
        CartItem item = findItem(product.getId());
        boolean isNew = item == null;
        if (isNew) {
            item = new CartItem();
            item.setProduct(product);
            item.setCart(this);
        }
        requireStock(item);
        if (isNew) {
            cartItems.add(item);
        }
        item.setQuantity(item.getQuantity() + 1);
        refreshTotal();
        return item;
    }

    public void increment(Long productId) {
        CartItem item = requireItem(productId);
        requireStock(item);
        item.setQuantity(item.getQuantity() + 1);
        refreshTotal();
    }

    /** Removes the line when its quantity would drop to zero. */
    public void decrement(Long productId) {
        CartItem item = requireItem(productId);
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cartItems.remove(item);
        }
        refreshTotal();
    }

    /** No-op when the product is not in the cart. */
    public void remove(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        refreshTotal();
    }

    public void clear() {
        cartItems.clear();
        refreshTotal();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public BigDecimal total() {
        return cartItems.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    @PreUpdate
    void refreshTotal() {
        this.totalPrice = total();
    }

    private CartItem findItem(Long productId) {
        return cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElse(null);
    }

    private CartItem requireItem(Long productId) {
        CartItem item = findItem(productId);
        if (item == null) {
            throw new ProductNotFoundException("Product with id " + productId + " does not exist in cart");
        }
        return item;
    }

    private static void requireStock(CartItem item) {
        Product product = item.getProduct();
        int available = Objects.requireNonNullElse(product.getAvailableQuantity(), 0); // column is nullable
        if (available < item.getQuantity() + 1) {
            throw new OutOfStockException("Product " + product.getProductName() + " is out of stock");
        }
    }
}
