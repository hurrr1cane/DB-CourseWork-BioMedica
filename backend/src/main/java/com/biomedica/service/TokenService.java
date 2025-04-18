package com.biomedica.service;

import com.biomedica.dto.AuthResponse;
import com.biomedica.entity.user.Token;
import com.biomedica.entity.user.TokenType;
import com.biomedica.entity.user.User;
import com.biomedica.exception.UnauthorizedException;
import com.biomedica.repository.TokenRepository;
import com.biomedica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public void saveUserToken(User user, String jwtToken, TokenType type) {
        log.info("Saving token for user with ID: {} and token: {}", user.getId(), jwtToken);

        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(type)
                .build();
        tokenRepository.save(token);

        log.info("Token saved successfully for user with ID: {}", user.getId());
    }

    public void revokeAllUserTokens(User user) {
        log.info("Revoking all tokens for user with ID: {}", user.getId());

        List<Token> tokens = tokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.info("No valid tokens found for user with ID: {}", user.getId());
            return;
        }

        tokenRepository.deleteAll(tokens);

        log.info("All tokens revoked for user with ID: {}", user.getId());
    }

    @Transactional
    public AuthResponse refreshToken(String authHeader) {
        log.info("Refreshing token with Authorization header: {}", authHeader);

        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Missing or invalid Authorization header: {}", authHeader);
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        refreshToken = authHeader.substring(7);
        tokenRepository.findByTokenAndTokenType(refreshToken, TokenType.REFRESH)
                .orElseThrow(() -> new UnauthorizedException("Your refresh token is invalid"));


        userEmail = jwtService.extractEmail(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userEmail));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                saveUserToken(user, accessToken, TokenType.ACCESS);

                log.info("Token refreshed successfully for user with email: {}", userEmail);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }

        log.error("Invalid refresh token for user with email: {}", userEmail);
        throw new UnauthorizedException("Invalid refresh token");
    }
}
