package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.*;
import org.example.ecommerce.Service.CheckoutService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class CheckoutServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser("test123");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test123", "test123"));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void checkout_updatesStatusAndDecreasesStock() {
        Product product = createProduct("Book", 5);
        Order order = createOrderWithItem(user, product, 2);

        ShowCheckoutOrderDto dto = checkoutService.checkout(order.getId());

        assertThat(dto.orderStatus()).isEqualTo(OrderStatus.IN_PROGRESS.name());
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void checkout_whenOutOfStock_throwsException() {
        Product product = createProduct("Book", 1);
        Order order = createOrderWithItem(user, product, 2);

        assertThrows(OutOfStockException.class, () -> checkoutService.checkout(order.getId()));
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    private Address createAddress(User user) {
        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress("Street 1");
        address.setCity("City");
        address.setState("State");
        address.setPostalCode("12345");
        address.setCountry("Country");
        return addressRepository.save(address);
    }

    private Product createProduct(String name, int qty) {
        Category category = new Category();
        category.setCategoryName("Books");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setProductName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(qty);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private Order createOrderWithItem(User user, Product product, int quantity) {
        Address address = createAddress(user);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setShippingType(ShippingType.STANDARD);
        order.setShippingCost(new BigDecimal("4.99"));
        order.setTotalCostAmount(new BigDecimal("10.00"));
        order = orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        orderItemRepository.save(item);

        order.setOrderItems(List.of(item));
        return orderRepository.save(order);
    }
}
