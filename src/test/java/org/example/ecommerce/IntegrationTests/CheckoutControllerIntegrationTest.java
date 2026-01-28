package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.*;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CheckoutControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @AfterEach
    void cleanUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void checkout_updatesOrderStatus() {
        String jwt = loginAndGetJwt("test123", "test123");
        User user = userRepository.findByUsername("test123").orElseThrow();
        Product product = createProduct("Book", 5);
        Order order = createOrderWithItem(user, product, 1);

        ResponseEntity<ShowCheckoutOrderDto> response = testRestTemplate.exchange(
                "/api/checkout/" + order.getId(),
                HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(jwt)),
                ShowCheckoutOrderDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().orderStatus()).isEqualTo(OrderStatus.IN_PROGRESS.name());
    }

    private String loginAndGetJwt(String username, String password) {
        registerUser(username, password);

        UserLoginDto loginDto = new UserLoginDto(username, password);

        ResponseEntity<JwtResponseDto> response = testRestTemplate.postForEntity(
                "/api/auth/login",
                loginDto,
                JwtResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().jwtToken();
    }

    private void registerUser(String username, String password) {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                username,
                username + "@example.com",
                password
        );

        ResponseEntity<RegisteredUserDto> response = testRestTemplate.postForEntity(
                "/api/auth/registration",
                registrationDto,
                RegisteredUserDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private HttpHeaders bearerHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);
        return headers;
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
