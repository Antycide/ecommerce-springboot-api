package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.Mappers.UserMapper;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Exception.UserAlreadyExistsException;
import org.example.ecommerce.Jwt.JwtUtils;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private Authentication authentication;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;

    private UserRegistrationDto validUserDto;
    private UserLoginDto userLoginDto;
    private User user;

    @BeforeEach
    void setUp() {
        validUserDto = new UserRegistrationDto(
                "testuser",
                "test@gmail.com",
                "password123"
        );

        userLoginDto = new UserLoginDto(
                "testuser",
                "password123"
        );

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);
    }

    @Test
    void registerUserWhenValidUserShouldReturnCreated() {
        // Given
        when(userRepository.findByUsername(validUserDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validUserDto.email())).thenReturn(Optional.empty());
        when(userMapper.toUser(validUserDto)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        ResponseEntity<String> response = authenticationService.registerUser(validUserDto);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("User Created Successfully");

        verify(userRepository).findByUsername(validUserDto.username());
        verify(userRepository).findByEmail(validUserDto.email());
        verify(userMapper).toUser(validUserDto);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);

    }

    @Test
    void registerUserWithExistingUsernameShouldThrowUserAlreadyExistsException() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("testuser");
        when(userRepository.findByUsername(validUserDto.username())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(validUserDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with username testuser already exists");

        verify(userRepository).findByUsername(validUserDto.username());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserWithExistingEmailShouldThrowUserAlreadyExistsException() {
        // Given
        User existingUser = new User();
        existingUser.setEmail("test@gmail.com");
        when(userRepository.findByUsername(validUserDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validUserDto.email())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(validUserDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email test@gmail.com already exists");

        verify(userRepository).findByUsername(validUserDto.username());
        verify(userRepository).findByEmail(validUserDto.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserShouldEncodePasswordBeforeSaving() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByUsername(validUserDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validUserDto.email())).thenReturn(Optional.empty());
        when(userMapper.toUser(validUserDto)).thenReturn(user);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        authenticationService.registerUser(validUserDto);

        // Then
        verify(passwordEncoder).encode(rawPassword);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        verify(userRepository).save(user);
    }

    @Test
    void registerUserShouldCallUserMapperToConvertDto() {
        // Given
        when(userRepository.findByUsername(validUserDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validUserDto.email())).thenReturn(Optional.empty());
        when(userMapper.toUser(validUserDto)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        authenticationService.registerUser(validUserDto);

        // Then
        verify(userMapper).toUser(validUserDto);
    }

    @Test
    void registerUserShouldCheckUsernameBeforeEmail() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("testuser");
        when(userRepository.findByUsername(validUserDto.username())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser(validUserDto))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository).findByUsername(validUserDto.username());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loginUserWhenValidUserShouldReturnJwtResponseDto() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("mockJwtToken");

        //when
        ResponseEntity<JwtResponseDto> response = authenticationService.loginUser(userLoginDto);

        //then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);

        assertEquals(200, response.getStatusCodeValue());
        JwtResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals("mockJwtToken", body.jwtToken());
        assertEquals("testuser", body.username());
        assertEquals(List.of("CUSTOMER"), body.roles());
    }

    @Test
    void testLoginUserFailedAuthenticationThrowsException() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // then
        assertThrows(RuntimeException.class, () -> authenticationService.loginUser(userLoginDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtils);
    }



}

