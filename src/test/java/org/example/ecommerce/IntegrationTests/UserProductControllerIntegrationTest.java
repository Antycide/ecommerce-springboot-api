package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.*;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserProductControllerIntegrationTest {

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
    void getAllProducts_returnsPage() {
        Category category = createCategory("Books");
        Product product = createProduct("Book", category);

        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
                "/api/products?page=0&size=5",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((List<?>) response.getBody().get("content"))).isNotEmpty();
    }

    @Test
    void getProductById_returnsProduct() {
        Category category = createCategory("Books");
        Product product = createProduct("Book", category);

        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<ShowProductDto> response = testRestTemplate.exchange(
                "/api/products/" + product.getId(),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                ShowProductDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productName()).isEqualTo("Book");
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

    private Category createCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        return categoryRepository.save(category);
    }

    private Product createProduct(String name, Category category) {
        Product product = new Product();
        product.setProductName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(10);
        product.setCategory(category);
        return productRepository.save(product);
    }
}
