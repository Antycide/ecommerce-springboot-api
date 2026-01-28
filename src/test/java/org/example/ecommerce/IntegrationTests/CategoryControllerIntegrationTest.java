package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.*;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.CategoryRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CategoryControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUpdateDeleteCategory_flowWorks() {
        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<AdminCategoryDto> createResponse = testRestTemplate.exchange(
                "/api/categories",
                HttpMethod.POST,
                new HttpEntity<>("Books", textHeaders(jwt)),
                AdminCategoryDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().categoryName()).isEqualTo("Books");

        Long categoryId = createResponse.getBody().id();

        ResponseEntity<AdminCategoryDto> updateResponse = testRestTemplate.exchange(
                "/api/categories/" + categoryId,
                HttpMethod.PUT,
                new HttpEntity<>("Movies", textHeaders(jwt)),
                AdminCategoryDto.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().categoryName()).isEqualTo("Movies");

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/categories/" + categoryId,
                HttpMethod.DELETE,
                new HttpEntity<>(textHeaders(jwt)),
                String.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getAllAndGetById_returnsCategories() {
        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<AdminCategoryDto> createResponse = testRestTemplate.exchange(
                "/api/categories",
                HttpMethod.POST,
                new HttpEntity<>("Books", textHeaders(jwt)),
                AdminCategoryDto.class
        );

        Long categoryId = createResponse.getBody().id();

        ResponseEntity<AdminCategoryDto> getByIdResponse = testRestTemplate.exchange(
                "/api/categories/" + categoryId,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                AdminCategoryDto.class
        );

        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResponse.getBody()).isNotNull();
        assertThat(getByIdResponse.getBody().id()).isEqualTo(categoryId);

        ResponseEntity<List<AdminCategoryDto>> getAllResponse = testRestTemplate.exchange(
                "/api/categories",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                new ParameterizedTypeReference<List<AdminCategoryDto>>() {}
        );

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).hasSize(1);
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

    private HttpHeaders textHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBearerAuth(jwt);
        return headers;
    }
}
