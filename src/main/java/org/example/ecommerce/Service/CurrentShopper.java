package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * The one place that knows how the logged-in shopper is resolved.
 * Returns a managed User entity loaded inside the caller's transaction.
 * Throws AuthenticationCredentialsNotFoundException (→ 401) when nobody is logged in.
 */
@Component
@RequiredArgsConstructor
public class CurrentShopper {

    private final UserRepository userRepository;

    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("No logged-in user");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
