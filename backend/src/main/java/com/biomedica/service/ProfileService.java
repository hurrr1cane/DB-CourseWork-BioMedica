package com.biomedica.service;

import com.biomedica.dto.UpdateProfileRequest;
import com.biomedica.dto.UserDto;
import com.biomedica.dto.mapper.UserMapper;
import com.biomedica.entity.user.User;
import com.biomedica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final AuditService auditService;
    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final UserRepository userRepository;

    public UserDto getProfile() {
        User user = auditService.getPrincipal();

        return userMapper.toDto(user);
    }

    @Transactional
    public void setNewPassword(String email, String code, String newPassword) {
        verificationService.verifyResettingPassword(email, code);

        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Transactional
    public UserDto updateProfile(UpdateProfileRequest updateProfileRequest) {
        User user = auditService.getPrincipal();

        if (updateProfileRequest.getName() != null) {
            user.setName(updateProfileRequest.getName());
        }

        if (updateProfileRequest.getSurname() != null) {
            user.setSurname(updateProfileRequest.getSurname());
        }

        userRepository.save(user);

        return userMapper.toDto(user);
    }
}
