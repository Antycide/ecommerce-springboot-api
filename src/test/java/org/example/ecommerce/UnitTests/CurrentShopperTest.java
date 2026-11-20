package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.CurrentShopper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentShopperTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentShopper currentShopper = new CurrentShopper(userRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_whenNoAuthentication_throwsCredentialsNotFound() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, currentShopper::get);
    }

    @Test
    void get_whenAnonymous_throwsCredentialsNotFound() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThrows(AuthenticationCredentialsNotFoundException.class, currentShopper::get);
    }

    @Test
    void get_whenLoggedIn_returnsUser() {
        User user = new User();
        user.setUsername("test123");
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test123", "pw"));

        assertThat(currentShopper.get()).isSameAs(user);
    }
}
