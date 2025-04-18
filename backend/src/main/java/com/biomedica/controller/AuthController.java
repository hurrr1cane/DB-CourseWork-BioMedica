package com.biomedica.controller;

import com.biomedica.dto.AuthRequest;
import com.biomedica.dto.AuthResponse;
import com.biomedica.dto.RegisterRequest;
import com.biomedica.service.AuthService;
import com.biomedica.service.ProfileService;
import com.biomedica.service.TokenService;
import com.biomedica.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final VerificationService verificationService;
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest registerRequest
    ) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String email,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(verificationService.verifyEmail(email, code));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String email
    ) {
        verificationService.initializeResetPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/set-new-password")
    public ResponseEntity<Void> setNewPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword
    ) {
        profileService.setNewPassword(email, code, newPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest authRequest
    ) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(tokenService.refreshToken(request.getHeader(HttpHeaders.AUTHORIZATION)));
    }
}
