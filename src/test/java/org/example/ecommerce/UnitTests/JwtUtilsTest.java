package org.example.ecommerce.UnitTests;

import io.jsonwebtoken.ExpiredJwtException;
import org.example.ecommerce.Jwt.JwtUtils;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Base64;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtUtils jwtUtils;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);
    }

    @Test
    void testGenerateAndValidateJwtTokenWithMockAuthentication() {
        // given
        String secretKey = Base64.getEncoder().encodeToString("mysecretkey1234567890mysecretkey1234567890".getBytes());
        TestUtils.setField(jwtUtils, "jwtSecret", secretKey);
        TestUtils.setField(jwtUtils, "jwtExpiration", 3600000); // 1 hour

        // mock authentication
        when(authentication.getPrincipal()).thenReturn(user);

        // when
        String token = jwtUtils.generateJwtToken(authentication);

        // then
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testuser", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void testValidateJwtTokenInvalidTokenReturnsFalse() {
        String secretKey = Base64.getEncoder().encodeToString("mysecretkey1234567890mysecretkey1234567890".getBytes());
        TestUtils.setField(jwtUtils, "jwtSecret", secretKey);
        TestUtils.setField(jwtUtils, "jwtExpiration", 3600000);

        assertFalse(jwtUtils.validateJwtToken("invalid Token"));
    }

    @Test
    void testExpiredJwtToken() throws InterruptedException {
        // given
        String secretKey = Base64.getEncoder().encodeToString("mysecretkey1234567890mysecretkey1234567890".getBytes());
        TestUtils.setField(jwtUtils, "jwtSecret", secretKey);

        // set expiration to 1 second
        TestUtils.setField(jwtUtils, "jwtExpiration", 1000);

        // mock authentication
        when(authentication.getPrincipal()).thenReturn(user);

        // when
        String token = jwtUtils.generateJwtToken(authentication);
        assertNotNull(token);

        // wait until token expires
        Thread.sleep(1500);

        // then
        boolean valid = jwtUtils.validateJwtToken(token);
        assertFalse(valid, "Expired token should not be valid");

        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtils.getUserNameFromJwtToken(token);
        });
    }

}
