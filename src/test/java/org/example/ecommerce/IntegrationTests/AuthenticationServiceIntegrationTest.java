package org.example.ecommerce.IntegrationTests;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.ecommerce.DTO.RegisteredUserDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Exception.UserAlreadyExistsException;
import org.example.ecommerce.Jwt.JwtUtils;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
public class AuthenticationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void connectionEstablished(){
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.isCreated()).isTrue();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );

        RegisteredUserDto response = authenticationService.registerUser(userRegistrationDto);

        assertThat(userRepository.findByUsername(userRegistrationDto.username()).isPresent()).isTrue();
        assertThat(userRepository.findByEmail(userRegistrationDto.email()).isPresent()).isTrue();
        assertThat(response.username()).isEqualTo(userRegistrationDto.username());
    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );

        UserRegistrationDto userRegistrationDto2 = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );

        authenticationService.registerUser(userRegistrationDto);

        assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.registerUser(userRegistrationDto2));

    }

    @Test
    void shouldNotRegisterUserWithExistingEmail() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );

        UserRegistrationDto userRegistrationDto2 = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );

        authenticationService.registerUser(userRegistrationDto);

        assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.registerUser(userRegistrationDto2));

    }

    @Test
    void loginUserWhenValidUserShouldLoginSuccessfullyAndReturnJwtResponseDto() {
        // Register user
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );
        authenticationService.registerUser(registrationDto);

        // Login user
        UserLoginDto loginDto = new UserLoginDto(
                "testuser",
                "password123"
        );
        var response = authenticationService.loginUser(loginDto);

        // Basic assertions
        assertNotNull(response);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.roles()).isEqualTo(List.of("CUSTOMER"));

        // JWT assertions
        String token = response.jwtToken();
        assertThat(token).isNotBlank();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.getExpiration()).isAfter(new java.util.Date());
    }

    @Test
    void loginUserWithInvalidCredentialsShouldFail() {
        // First, register a valid user
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "testuser",
                "test123@gmail.com",
                "password123"
        );
        authenticationService.registerUser(registrationDto);

        // Attempt login with wrong password
        UserLoginDto wrongPasswordDto = new UserLoginDto(
                "testuser",
                "wrongPassword"
        );

        assertThrows(BadCredentialsException.class, () -> authenticationService.loginUser(wrongPasswordDto));

        // Attempt login with non-existent username
        UserLoginDto nonExistentUserDto = new UserLoginDto(
                "unknownuser",
                "password123"
        );

        assertThrows(BadCredentialsException.class, () -> authenticationService.loginUser(nonExistentUserDto));
    }
}
