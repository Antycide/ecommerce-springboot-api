package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.ShowCartDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartControllerIntegrationTest {

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
    void cartFlow_addIncreaseDecreaseDelete() {
        Product product = createProduct("Book");
        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<String> addResponse = testRestTemplate.exchange(
                "/api/cart/" + product.getId(),
                HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ShowCartDto> showResponse = testRestTemplate.exchange(
                "/api/cart",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                ShowCartDto.class
        );

        assertThat(showResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(showResponse.getBody()).isNotNull();
        assertThat(showResponse.getBody().cart()).hasSize(1);

        ResponseEntity<String> increaseResponse = testRestTemplate.exchange(
                "/api/cart/increase/" + product.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(increaseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> decreaseResponse = testRestTemplate.exchange(
                "/api/cart/decrease/" + product.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(decreaseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> deleteItemResponse = testRestTemplate.exchange(
                "/api/cart/" + product.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(deleteItemResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> clearCartResponse = testRestTemplate.exchange(
                "/api/cart",
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(clearCartResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private String loginAndGetJwt(String username, String password) {
        registerUser(username, password);

        UserLoginDto loginDto = new UserLoginDto(username, password);

        ResponseEntity<JwtResponseDto> response = testRestTemplate.postForEntity(
                "/api/v1/auth/login",
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

        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/v1/auth/registration",
                registrationDto,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
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
