package com.biomedica.service;

import com.biomedica.entity.user.User;
import com.biomedica.entity.user.VerificationEntity;
import com.biomedica.entity.user.VerificationType;
import com.biomedica.repository.UserRepository;
import com.biomedica.repository.VerificationEntityRepository;
import com.biomedica.utils.ServiceUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final EmailService emailService;
    private final ServiceUtils serviceUtils;
    private final VerificationEntityRepository verificationEntityRepository;
    private final UserRepository userRepository;

    public void sendVerificationCode(User user, VerificationType type) {

        VerificationEntity verificationEntity = VerificationEntity.builder()
                .user(user)
                .code(serviceUtils.generateRandomCode())
                .verificationType(type)
                .expirationDate(OffsetDateTime.now().plusMinutes(5))
                .build();

        // send email with code
        emailService.sendEmail(user.getEmail(), verificationEntity.getCode());

        verificationEntityRepository.save(verificationEntity);
    }

    public String verifyEmail(String email, String code) {
        VerificationEntity verificationEntity = verificationEntityRepository.findByUserEmailAndVerificationType(email, VerificationType.REGISTRATION)
                .orElseThrow(() -> new EntityNotFoundException("Verification code not found or invalid"));

        if (!verificationEntity.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (verificationEntity.getExpirationDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        // Assuming there's a method to mark the email as verified
        verificationEntity.getUser().setVerified(true);
        userRepository.save(verificationEntity.getUser());

        verificationEntityRepository.delete(verificationEntity);

        return "Email verified";
    }

    @Scheduled(fixedRate = 60000)
    public void deleteExpiredVerificationCodes() {
        verificationEntityRepository.deleteByExpirationDateBefore(OffsetDateTime.now());
    }

    public void initializeResetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User is not found"));
        VerificationEntity verificationEntity = VerificationEntity.builder()
                .user(user)
                .code(serviceUtils.generateRandomCode())
                .verificationType(VerificationType.PASSWORD_RESET)
                .expirationDate(OffsetDateTime.now().plusMinutes(5))
                .build();

        emailService.sendEmail(email, verificationEntity.getCode());

        verificationEntityRepository.save(verificationEntity);
    }

    public void verifyResettingPassword(String email, String code) {
        VerificationEntity verificationEntity = verificationEntityRepository.findByUserEmailAndVerificationType(email, VerificationType.PASSWORD_RESET)
                .orElseThrow(() -> new EntityNotFoundException("Verification code not found or invalid"));

        if (!verificationEntity.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (verificationEntity.getExpirationDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        verificationEntityRepository.delete(verificationEntity);

    }

}
