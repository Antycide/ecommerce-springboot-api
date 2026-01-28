package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.*;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WishlistControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addAndDeleteWishlistItem_flowWorks() {
        Product product = createProduct("Book");
        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<ShowWishlistDto> addResponse = testRestTemplate.exchange(
                "/api/wishlist/" + product.getId(),
                HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(jwt)),
                ShowWishlistDto.class
        );

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(addResponse.getBody()).isNotNull();

        ResponseEntity<WishlistResponseDto> getResponse = testRestTemplate.exchange(
                "/api/wishlist",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                WishlistResponseDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().wishlist()).hasSize(1);

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/wishlist/" + product.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void showWishlistOfAnotherUser_returnsEmptyList() {
        User other = registerUserEntity("other", "other@example.com");
        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<WishlistResponseDto> response = testRestTemplate.exchange(
                "/api/wishlist/" + other.getId(),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                WishlistResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().wishlist()).isEmpty();
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

    private User registerUserEntity(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    private HttpHeaders bearerHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);
        return headers;
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
}
