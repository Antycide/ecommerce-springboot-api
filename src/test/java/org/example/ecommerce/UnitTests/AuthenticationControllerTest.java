package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.AuthenticationController;
import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Exception.UserAlreadyExistsException;
import org.example.ecommerce.Service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;


    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test",
                "test123@gmail.com",
                "password123"
        );

        when(authenticationService.registerUser(userRegistrationDto))
                .thenReturn(new ResponseEntity<>("User Created Successfully", HttpStatus.CREATED));

        mockMvc.perform(post("/api/v1/auth/registration").
                contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "test",
                    "email": "test123@gmail.com",
                    "password": "password123"
                }
            """)).andExpect(status().isCreated())
                .andExpect(content().string("User Created Successfully"));
        verify(authenticationService, times(1)).registerUser(userRegistrationDto);

    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingUsername() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test",
                "test123@gmail.com",
                "password123"
        );

        when(authenticationService.registerUser(any()))
                .thenThrow(new UserAlreadyExistsException("User with username test already exists"));

        mockMvc.perform(post("/api/v1/auth/registration").
                        contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "username": "test",
                    "email": "test123@gmail.com",
                    "password": "password123"
                }
            """)).andExpect(status().isBadRequest())
                        .andExpect(content().string("User with username test already exists"));
        verify(authenticationService, times(1)).registerUser(userRegistrationDto);

    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingEmail() throws Exception {
        when(authenticationService.registerUser(any()))
                .thenThrow(new UserAlreadyExistsException("User with email test123@gmail.com already exists"));

        mockMvc.perform(post("/api/v1/auth/registration").
                contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "test2",
                    "email": "test123@gmail.com",
                    "password": "password123"
                }
            """)).andExpect(status().isBadRequest())
                        .andExpect(content().string("User with email test123@gmail.com already exists"));
        verify(authenticationService, times(1)).registerUser(any());
    }

    @Test
    void loginUserWhenValidUserShouldReturnJwtResponseDto() throws Exception {
        UserLoginDto userLoginDto = new UserLoginDto(
                "test123"
        ,"password123");

        JwtResponseDto jwtResponseDto = new JwtResponseDto("mockJwtToken",
                "test123",
                List.of("CUSTOMER"));

        when(authenticationService.loginUser(userLoginDto)).thenReturn(ResponseEntity.ok(jwtResponseDto));

        mockMvc.perform(post("/api/v1/auth/login").
                contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "test123",
                    "password": "password123"
                }
            """)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwtToken").value("mockJwtToken"))
                .andExpect(jsonPath("$.username").value("test123"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    @Test
    void loginUserWhenInvalidUserShouldReturn401() throws Exception {

        when(authenticationService.loginUser(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login").
                contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "test123",
                    "password": "password123"
                }
            """)).andExpect(status().isUnauthorized());
        verify(authenticationService, times(1)).loginUser(any());
    }

    @Test
    void loginUserWithBlankUsernameShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").
                contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "",
                    "password": "password123"
                }
            """)).andExpect(status().isBadRequest());
        verify(authenticationService, never()).loginUser(any());
    }

    @Test
    void loginUserWithBlankPasswordShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").
                contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "test123",
                    "password": ""
                }
                """)
        ).andExpect(status().isBadRequest());
        verify(authenticationService, never()).loginUser(any());
    }

}
