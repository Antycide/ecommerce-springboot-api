package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.Exception.ProductInUseException;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.*;
import org.example.ecommerce.Service.AdminProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductDeletionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired private AdminProductService adminProductService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    // Runs first so the container is fresh: the first product and the first order both get id 1.
    @Test
    @org.junit.jupiter.api.Order(1)
    void deleteProduct_doesNotDetachItemsOfOrderWithSameId() {
        Category category = category();
        Product unrelated = productRepository.save(product("Unrelated", category));
        Product ordered = productRepository.save(product("Ordered", category));
        Order order = orderOf(user("u1"), ordered);
        // Precondition, not an assumption: if ids stop colliding this test must fail, not silently skip.
        assertThat(order.getId()).as("order id must equal the deleted product's id").isEqualTo(unrelated.getId());
        Long orderId = order.getId();

        adminProductService.deleteProduct(unrelated.getId());

        assertThat(orderItemRepository.findAll())
                .singleElement()
                .satisfies(i -> assertThat(i.getOrder().getId()).isEqualTo(orderId));
    }

    @Test
    void deleteProduct_whenInOrder_throwsProductInUseAndKeepsProduct() {
        Product product = productRepository.save(product("Ordered", category()));
        orderOf(user("u2"), product);

        assertThrows(ProductInUseException.class, () -> adminProductService.deleteProduct(product.getId()));
        assertThat(productRepository.existsById(product.getId())).isTrue();
    }

    @Test
    void deleteProduct_whenOnlyInCart_removesItFromCart() {
        Product product = productRepository.save(product("Carted", category()));
        Cart cart = new Cart();
        cart.setUser(user("u3"));
        cart = cartRepository.save(cart);
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        cartItemRepository.save(item);

        adminProductService.deleteProduct(product.getId());

        assertThat(productRepository.existsById(product.getId())).isFalse();
        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    private Category category() {
        Category category = new Category();
        category.setCategoryName("Books");
        return categoryRepository.save(category);
    }

    private User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("p");
        user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    private Order orderOf(User user, Product product) {
        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress("s");
        address.setCity("c");
        address.setCountry("BG");
        address = addressRepository.save(address);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setShippingType(ShippingType.STANDARD);
        order.setShippingCost(new BigDecimal("4.99"));
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        order.addOrderItem(item);
        return orderRepository.save(order);
    }

    private static Product product(String name, Category category) {
        Product p = new Product();
        p.setProductName(name);
        p.setPrice(new BigDecimal("10.00"));
        p.setAvailableQuantity(5);
        p.setCategory(category);
        return p;
    }
}
