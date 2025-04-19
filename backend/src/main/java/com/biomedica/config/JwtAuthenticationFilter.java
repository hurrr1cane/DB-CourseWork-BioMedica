package com.biomedica.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.biomedica.dto.ExceptionResponse;
import com.biomedica.entity.user.TokenType;
import com.biomedica.entity.user.User;
import com.biomedica.repository.TokenRepository;
import com.biomedica.repository.UserRepository;
import com.biomedica.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        if (isWhitelisted(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (isInvalidAuthHeader(authHeader, request, response, filterChain)) {
            return;
        }

        final String jwt = authHeader.substring(7);
        String email;
        try {
            email = jwtService.extractEmail(jwt);
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", jwt);
            setErrorResponse(
                    response,
                    ExceptionResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Your token is expired")
                            .build());
            return;
        } catch (MalformedJwtException | SignatureException ex) {
            log.warn("JWT token is invalid: {}", jwt);
            setErrorResponse(
                    response,
                    ExceptionResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .message("Your token is invalid")
                            .build());
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Processing JWT token for user: {}", email);
            processTokenAuthentication(request, response, filterChain, jwt, email);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isInvalidAuthHeader(String authHeader, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (!isWhitelisted(request.getServletPath())) {
                log.warn("Invalid Authorization header: {}", authHeader);
                setErrorResponse(
                        response,
                        ExceptionResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .message("Missing or invalid Authorization header")
                                .build());
                return true;
            }
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }

    private void processTokenAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String jwt, String email) throws IOException, ServletException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        var isTokenValid = tokenRepository.findByTokenAndTokenType(jwt, TokenType.ACCESS)
                .map(t -> t.getUser().isEnabled() && jwtService.isTokenValid(jwt, userDetails))
                .orElse(false);

        var user = userRepository.findByEmail(email).orElseThrow(
                () -> {
                    log.error("User not found with email: {}", email);
                    return new EntityNotFoundException("User not found with email: " + email);
                }
        );

        if (!user.isEnabled() || !jwtService.isTokenValid(jwt, userDetails) || Boolean.TRUE.equals(!isTokenValid)) {
            handleInvalidToken(response, request.getServletPath(), user);
            return;
        }

        setAuthentication(request, userDetails);
        filterChain.doFilter(request, response);
    }

    private void handleInvalidToken(HttpServletResponse response, String servletPath, User user) throws IOException {
        if (!isWhitelisted(servletPath)) {
            log.warn("Invalid token or user status for path: {}", servletPath);
            setErrorResponse(
                    response,
                    ExceptionResponse.builder()
                            .status(user.isEnabled() ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN)
                            .message(user.isEnabled() ? "Invalid token" : "User is disabled")
                            .build());
        }
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        log.info("Setting authentication for user: {}", userDetails.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setErrorResponse(HttpServletResponse response, ExceptionResponse exceptionResponse) throws IOException {
        log.error("Setting error response: {}", exceptionResponse);
        response.setStatus(exceptionResponse.getStatus().value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
    }

    private boolean isWhitelisted(String servletPath) {
        return Arrays.stream(SecurityConfig.WHITE_LIST_URIS)
                .anyMatch(whiteListedUri -> pathMatcher.match(whiteListedUri, servletPath));
    }
}