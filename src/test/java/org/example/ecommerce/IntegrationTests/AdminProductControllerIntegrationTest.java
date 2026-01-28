package org.example.ecommerce.IntegrationTests;

import jakarta.transaction.Transactional;
import org.example.ecommerce.DTO.*;
import org.example.ecommerce.Model.Address;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminProductControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;


    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProduct_whenValid_createsProduct_andCanFetchIt() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");

        createCategory("cat");

        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<ShowAdminProductDto> response = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createProduct_whenCategoryDoesntExist_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");

        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<String> response = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Category with name cat does not exist");
    }

    @Test
    void createProduct_whenProductNameIsEmpty_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");

        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<String> response = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Product name cannot be empty");
    }

    @Test
    void createProduct_whenDuplicateProduct_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");

        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<ShowAdminProductDto> create = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );

        ResponseEntity<String> duplicateResponse = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                String.class
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicateResponse.getBody()).contains("Product with name name already exists");
    }

    @Test
    void deleteProduct_whenProductExists_deletesProduct() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");
        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<ShowAdminProductDto> createResponse = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );
        assertThat(createResponse.getBody()).isNotNull();
        Long productId = createResponse.getBody().id();

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/admin/products/" + productId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(productRepository.findAll()).isEmpty();
        assertThat(deleteResponse.getBody()).isNull();
    }

    @Test
    void deleteProduct_whenProductDoesntExist_throwsException() {
        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/admin/products/999999",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteResponse.getBody()).contains("Product with id 999999 does not exist");
    }

    @Test
    void getProductById_whenProductExists_returnsProduct() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");
        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<ShowAdminProductDto> createResponse = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );
        assertThat(createResponse.getBody()).isNotNull();
        Long productId = createResponse.getBody().id();

        ResponseEntity<ShowAdminProductDto> product = testRestTemplate.exchange(
                "/api/admin/products/" + productId,
                HttpMethod.GET,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );

        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(product.getBody()).isNotNull();
        assertThat(product.getBody().productName()).isEqualTo(addProductDto.productName());
    }

    @Test
    void getProductById_whenProductDoesntExist_throwsException() {
        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);
        ResponseEntity<String> product = testRestTemplate.exchange(
                "/api/admin/products/999999",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(product.getBody()).contains("Product with id 999999 does not exist");
    }

    @Test
    void updateProduct_whenProductExists_updatesProduct() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");
        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<ShowAdminProductDto> createResponse = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );
        assertThat(createResponse.getBody()).isNotNull();
        Long productId = createResponse.getBody().id();

        AddProductDto updatedProduct = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");

        ResponseEntity<ShowAdminProductDto> product = testRestTemplate.exchange(
                "/api/admin/products/" + productId,
                HttpMethod.PUT,
                new HttpEntity<>(updatedProduct, headers),
                ShowAdminProductDto.class
        );

        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(product.getBody()).isNotNull();
        assertThat(product.getBody().productName()).isEqualTo(updatedProduct.productName());
    }

    @Test
    void updateProduct_whenProductDoesntExist_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");
        String jwt = loginAndGetJwt("test123", "test123");

        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<String> product = testRestTemplate.exchange(
                "/api/admin/products/999999",
                HttpMethod.PUT,
                new HttpEntity<>(addProductDto, headers),
                String.class
        );

        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(product.getBody()).contains("Product with id 999999 does not exist");
    }

    @Test
    void updateProduct_whenCategoryDoesntExist_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");
        String jwt = loginAndGetJwt("test123", "test123");

        HttpHeaders headers = bearerHeaders(jwt);

        ResponseEntity<ShowAdminProductDto> createResponse = testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );
        assertThat(createResponse.getBody()).isNotNull();
        Long productId = createResponse.getBody().id();

        AddProductDto updatedDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "car");

        ResponseEntity<String> product = testRestTemplate.exchange(
                "/api/admin/products/" + productId,
                HttpMethod.PUT,
                new HttpEntity<>(updatedDto, headers),
                String.class
        );

        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(product.getBody()).contains("Category with name car does not exist");
    }

    @Test
    void getAllProducts_whenProductsExist_returnsAllProducts() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createCategory("cat");
        String jwt = loginAndGetJwt("test123", "test123");
        HttpHeaders headers = bearerHeaders(jwt);

        testRestTemplate.exchange(
                "/api/admin/products",
                HttpMethod.POST,
                new HttpEntity<>(addProductDto, headers),
                ShowAdminProductDto.class
        );

        ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
                "/api/admin/products?page=0&size=5",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(((List<?>) response.getBody().get("content"))).isNotEmpty();
    }


    private String loginAndGetJwt(String username, String password) {
        registerUser(username, password);

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
        });

        UserLoginDto loginDto = new UserLoginDto(username, password);

        ResponseEntity<JwtResponseDto> response = testRestTemplate.postForEntity(
                "/api/auth/login",
                loginDto,
                JwtResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().jwtToken()).isNotBlank();

        return response.getBody().jwtToken();
    }

    private void registerUser(String username, String password) {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                username,
                username + "@example.com",
                password
        );

        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/auth/registration",
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

    private void createCategory(String categoryName) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        categoryRepository.save(category);
    }
}
