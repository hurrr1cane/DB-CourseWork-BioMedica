package com.biomedica.service;

import com.biomedica.dto.AuthRequest;
import com.biomedica.dto.AuthResponse;
import com.biomedica.dto.RegisterRequest;
import com.biomedica.entity.user.Patient;
import com.biomedica.entity.user.User;
import com.biomedica.entity.user.TokenType;
import com.biomedica.entity.user.VerificationType;
import com.biomedica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("User already exists with email: {}", request.getEmail());
            throw new IllegalArgumentException("User already exists with email: " + request.getEmail());
        }

        Patient patient = new Patient();
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setName(request.getName());
        patient.setSurname(request.getSurname());

        User savedUser = userRepository.save(patient);
        log.info("User successfully registered with ID: {}", savedUser.getId());

        String jwtToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        tokenService.saveUserToken(savedUser, jwtToken, TokenType.ACCESS);
        tokenService.saveUserToken(savedUser, refreshToken, TokenType.REFRESH);
        log.debug("Generated and saved JWT token for user ID: {}", savedUser.getId());

        verificationService.sendVerificationCode(savedUser, VerificationType.REGISTRATION);
        log.debug("Verification code sent to email: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Attempting to log in user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", request.getEmail());
                    return new EntityNotFoundException("User not found with email: " + request.getEmail());
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for user with email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        log.info("User authenticated successfully with email: {}", request.getEmail());

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, jwtToken, TokenType.ACCESS);
        tokenService.saveUserToken(user, refreshToken, TokenType.REFRESH);
        log.debug("Generated and saved new JWT token for user ID: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

}
