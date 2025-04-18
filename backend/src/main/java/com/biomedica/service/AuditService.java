package com.biomedica.service;

import com.biomedica.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditService {

    /**
     * Retrieves the currently authenticated user (principal) from the security context.
     *
     * @return the authenticated {@link User}.
     * @throws SecurityException if the authentication context is empty or the user is not authenticated.
     */
    public User getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("Failed to retrieve the authentication object from the security context. Authentication is null.");
            throw new SecurityException("Authentication context is empty. Can't extract principal.");
        }

        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("User is not authenticated or the authentication is anonymous. Authentication: {}", authentication);
            throw new SecurityException("Authentication context is empty. Can't extract principal.");
        }

        User user = (User) authentication.getPrincipal();
        log.debug("Successfully retrieved authenticated user: {}", user.getUsername());

        return user;
    }
}
