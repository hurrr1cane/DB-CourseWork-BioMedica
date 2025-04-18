package com.biomedica.config;

import com.biomedica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

    private final UserRepository userRepository;


    @Bean
    public UserDetailsService userDetailsService() {
        return email -> (UserDetails) userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User not found with email: " + email)
        );
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        return authenticationProvider;
    }

    /**
     * Defines the PasswordEncoder bean used for encoding passwords.
     * BCrypt is a strong hashing function used to store encrypted passwords.
     *
     * @return A PasswordEncoder for encoding passwords using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
