package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.RegisteredUserDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Exception.UserAlreadyExistsException;
import org.example.ecommerce.Repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthenticationControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @Rollback
    void shouldRegisterUserSuccessfully() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test",
                "test123@gmail.com",
                "password123"
        );

        ResponseEntity<RegisteredUserDto> response = testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto,
                RegisteredUserDto.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("test");
        assertThat(response.getBody().id()).isEqualTo(1);

    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test1",
                "test123@gmail.com",
                "password123"
        );

        // First registration - should succeed
        ResponseEntity<RegisteredUserDto> firstResponse = testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto,
                RegisteredUserDto.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UserRegistrationDto userRegistrationDto2 = new UserRegistrationDto(
                "test1",
                "different@gmail.com", // Different email, same username
                "password123"
        );


        ResponseEntity<String> secondResponse = testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto2,
                String.class
        );


        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(secondResponse.getBody()).isEqualTo("User with username test1 already exists");
    }

    @Test
    void shouldNotRegisterUserWithExistingEmail() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test3",
                "test123@gmail.com",
                "password123"
        );

        // First registration - should succeed
        ResponseEntity<RegisteredUserDto> firstResponse = testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto,
                RegisteredUserDto.class
        );

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UserRegistrationDto userRegistrationDto2 = new UserRegistrationDto(
                "test4", //different username, same email
                "test123@gmail.com",
                "password123"
        );


        ResponseEntity<String> secondResponse = testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto2,
                String.class
        );


        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(secondResponse.getBody()).isEqualTo("User with email test123@gmail.com already exists");
    }

    @Test
    void shouldLoginValidUserAndReturnJwtResponseDto() {

        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "testuser",
                "testuser@gmail.com",
                "password123"
        );

        testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto,
                String.class
        );

        // Now attempt to login with correct credentials
        UserLoginDto userLoginDto = new UserLoginDto(
                "testuser",
                "password123"
        );

        ResponseEntity<JwtResponseDto> response = testRestTemplate.postForEntity(
                "/api/auth/login",
                userLoginDto,
                JwtResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().jwtToken()).isNotBlank();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        assertThat(response.getBody().roles()).isEqualTo(List.of("CUSTOMER"));
    }

    @Test
    void shouldReturn401WhenLoginWithInvalidCredentials() {
        // First, register a user
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "testuser2",
                "testuser2@gmail.com",
                "password123"
        );

        testRestTemplate.postForEntity(
                "/api/auth/registration",
                userRegistrationDto,
                String.class
        );

        // Attempt to login with wrong password
        UserLoginDto userLoginDto = new UserLoginDto(
                "testuser2",
                "wrongpassword"
        );

        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/auth/login",
                userLoginDto,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenLoginWithNonExistentUser() {
        // Attempt to login with a user that doesn't exist
        UserLoginDto userLoginDto = new UserLoginDto(
                "nonexistent",
                "password123"
        );

        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/auth/login",
                userLoginDto,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
