package com.biomedica.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider provider;
    private final JwtAuthenticationFilter filter;

    public static final String[] WHITE_LIST_URIS = {
            "/api/auth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/actuator/**",
            "/api/laboratories**"
    };

    /**
     * Configures the security filter chain with rules and filters.
     *
     * @param http The HttpSecurity object to configure.
     * @return A configured SecurityFilterChain.
     * @throws Exception If an error occurs while configuring security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()  // Authentication endpoints for everyone
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRATOR")  // Only ADMINs
                .requestMatchers("/api/lab/**").hasRole("LABORATORY_ASSISTANT")  // Only LABORATORY_ASSISTANT
                .requestMatchers("/api/patient/**").hasRole("PATIENT")  // Only CUSTOMER
                .requestMatchers(WHITE_LIST_URIS).permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(provider)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     *
     * @return CorsConfigurationSource object containing CORS configuration.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("Cache-Control");
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("x-requested-with");
        configuration.addAllowedHeader("x-app-version");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
