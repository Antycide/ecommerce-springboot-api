package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.*;
import org.example.ecommerce.Service.OrderService;
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

@Testcontainers
@SpringBootTest
public class OrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

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
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createOrder_createsOrderAndClearsCart() {
        Address address = createAddress(user);
        Product product = createProduct("Book");
        createCartWithItem(user, product, 2);

        ShowOrderDto dto = orderService.createOrder(new CreateOrderRequest(address.getId(), ShippingType.STANDARD));

        assertThat(dto).isNotNull();
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    @Test
    void getAllOrdersOfCurrentUser_returnsOrders() {
        Address address = createAddress(user);
        Product product = createProduct("Book");
        createCartWithItem(user, product, 1);
        orderService.createOrder(new CreateOrderRequest(address.getId(), ShippingType.STANDARD));

        List<ShowOrderDto> orders = orderService.getAllOrdersOfCurrentUser();

        assertThat(orders).hasSize(1);
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

    private Product createProduct(String name) {
        Category category = new Category();
        category.setCategoryName("Books");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setProductName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(10);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private Cart createCartWithItem(User user, Product product, int quantity) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        cart = cartRepository.save(cart);
        user.setCart(cart);
        userRepository.save(user);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        cart.getCartItems().add(item);
        cartRepository.save(cart);
        return cart;
    }
}
