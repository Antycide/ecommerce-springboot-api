package org.example.ecommerce.IntegrationTests;

import org.assertj.core.api.AssertionsForClassTypes;
import org.example.ecommerce.DTO.AddAddressDto;
import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.ShowAddressDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Repository.AddressRepository;
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
public class AddressControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        addressRepository.deleteAll();
    }

    @Test
    void connectionEstablished(){
        AssertionsForClassTypes.assertThat(postgres.isRunning()).isTrue();
        AssertionsForClassTypes.assertThat(postgres.isCreated()).isTrue();
    }

    @Test
    void addAddress_createsAddress_andCanFetchIt() {
        String jwt = loginAndGetJwt("test123", "test123");

        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        // Act: POST /api/addresses with Bearer token
        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        // Assert: created
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().id()).isNotNull();
        assertThat(createResponse.getBody().streetAddress()).isEqualTo("Druzhba 40");

        Long addressId = createResponse.getBody().id();

        // Act: GET /api/addresses/{id} with Bearer token
        ResponseEntity<ShowAddressDto> getResponse = testRestTemplate.exchange(
                "/api/addresses/" + addressId,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        // Assert: fetched
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().id()).isEqualTo(addressId);
        assertThat(getResponse.getBody().city()).isEqualTo("Plovdiv");
    }

    @Test
    void addAddress_ifDuplicateAddress_throwsException() {
        String jwt = loginAndGetJwt("test123", "test123");

        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        // Act: POST /api/addresses with Bearer token
        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        // Assert: created
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().id()).isNotNull();
        assertThat(createResponse.getBody().streetAddress()).isEqualTo("Druzhba 40");

        ResponseEntity<String> duplicateResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                String.class
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicateResponse.getBody()).contains("Address already exists");
    }

    @Test
    void deleteAddress_deletesAddress_andCannotBeFetched() {
        String jwt = loginAndGetJwt("test123", "test123");
        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().city()).isEqualTo("Plovdiv");

        Long addressId = createResponse.getBody().id();

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/addresses/" + addressId,
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(addressRepository.findById(addressId)).isEmpty();
    }

    @Test
    void deleteAddress_ifMissing_throwsException() {
        String jwt = loginAndGetJwt("test123", "test123");
        Long addressId = 1000L;

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/addresses/" + addressId,
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteResponse.getBody()).contains("Address not found");
    }

    @Test
    void deleteAddress_ifNotOwner_throwsException() {
        String jwt = loginAndGetJwt("test123", "test123");
        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().city()).isEqualTo("Plovdiv");

        Long addressId = createResponse.getBody().id();

        String otherJwt = loginAndGetJwt("test1234", "test1234");

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                "/api/addresses/" + addressId,
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(otherJwt)),
                String.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(deleteResponse.getBody()).contains("You are not authorized to perform this action");
    }

    @Test
    void getAddressOfCurrentUserByAddressId_returnsAddressOfCurrentUser() {
        String jwt = loginAndGetJwt("test123", "test123");
        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().city()).isEqualTo("Plovdiv");
    }

    @Test
    void getAddressOfCurrentUserByAddressId_ifAddressNotFound_throwException() {
        String jwt = loginAndGetJwt("test123", "test123");
        Long addressId = 1000L;

        ResponseEntity<String> getResponse = testRestTemplate.exchange(
                "/api/addresses/" + addressId,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                String.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).contains("Address not found");
    }

    @Test
    void getAddressOfCurrentUserByAddressId_ifNotOwner_throwException() {
        String jwt = loginAndGetJwt("test123", "test123");
        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().city()).isEqualTo("Plovdiv");

        String otherJwt = loginAndGetJwt("test1234", "test1234");

        Long addressId = createResponse.getBody().id();

        ResponseEntity<String> getResponse = testRestTemplate.exchange(
                "/api/addresses/" + addressId,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(otherJwt)),
                String.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(getResponse.getBody()).contains("You are not authorized to perform this action");
    }

    @Test
    void getAllAddressesOfCurrentUser_returnsAllAddressesOfCurrentUser() {
        String jwt = loginAndGetJwt("test123", "test123");
        AddAddressDto dto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria"
        );

        ResponseEntity<ShowAddressDto> createResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.POST,
                new HttpEntity<>(dto, bearerHeaders(jwt)),
                ShowAddressDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().city()).isEqualTo("Plovdiv");


        ResponseEntity<List<ShowAddressDto>> getResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                new ParameterizedTypeReference<List<ShowAddressDto>>() {}
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).hasSize(1);
        assertThat(getResponse.getBody().get(0).city()).isEqualTo("Plovdiv");
    }

    @Test
    void getAllAddressesOfCurrentUser_ifEmtpy_returnsEmptyList() {
        String jwt = loginAndGetJwt("test123", "test123");

        ResponseEntity<List<ShowAddressDto>> getResponse = testRestTemplate.exchange(
                "/api/addresses",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(jwt)),
                new ParameterizedTypeReference<List<ShowAddressDto>>() {}
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEmpty();
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



}
