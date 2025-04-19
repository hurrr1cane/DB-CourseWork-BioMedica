package com.biomedica.service;

import com.biomedica.dto.RegisterLabAssistantRequest;
import com.biomedica.dto.UserDto;
import com.biomedica.dto.mapper.UserMapper;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto registerLabAssistant(RegisterLabAssistantRequest registerLabAssistantRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerLabAssistantRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(registerLabAssistantRequest.getPassword());

        // Create a new LabAssistant entity
        LaboratoryAssistant labAssistant = new LaboratoryAssistant();
        labAssistant.setEmail(registerLabAssistantRequest.getEmail());
        labAssistant.setPassword(hashedPassword);
        labAssistant.setName(registerLabAssistantRequest.getName());
        labAssistant.setSurname(registerLabAssistantRequest.getSurname());

        return userMapper.toDto(userRepository.save(labAssistant));
    }
}