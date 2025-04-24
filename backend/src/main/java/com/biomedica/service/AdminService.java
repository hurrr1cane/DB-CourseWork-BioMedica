package com.biomedica.service;

import com.biomedica.dto.LaboratoryAssistantDto;
import com.biomedica.dto.RegisterLabAssistantRequest;
import com.biomedica.dto.mapper.LaboratoryAssistantMapper;
import com.biomedica.dto.mapper.UserMapper;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.repository.LaboratoryAssistantRepository;
import com.biomedica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final LaboratoryAssistantRepository laboratoryAssistantRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LaboratoryAssistantMapper laboratoryAssistantMapper;

    public LaboratoryAssistantDto registerLabAssistant(RegisterLabAssistantRequest registerLabAssistantRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerLabAssistantRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(registerLabAssistantRequest.getPassword());

        // Create a new LabAssistant entity
        LaboratoryAssistant labAssistant = new LaboratoryAssistant();
        labAssistant.setEmail(registerLabAssistantRequest.getEmail());
        labAssistant.setPhoneNumber(registerLabAssistantRequest.getPhoneNumber());
        labAssistant.setPassword(hashedPassword);
        labAssistant.setName(registerLabAssistantRequest.getName());
        labAssistant.setSurname(registerLabAssistantRequest.getSurname());

        return laboratoryAssistantMapper.toDto(laboratoryAssistantRepository.save(labAssistant));
    }

    public Page<LaboratoryAssistantDto> getLaboratoryAssistants(Pageable pageable) {
        return laboratoryAssistantRepository.findAll(pageable).map(laboratoryAssistantMapper::toDto);
    }

    public LaboratoryAssistantDto getLaboratoryAssistantById(UUID id) {
        LaboratoryAssistant labAssistant = laboratoryAssistantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory Assistant not found"));
        return laboratoryAssistantMapper.toDto(labAssistant);
    }

    public LaboratoryAssistantDto updateLaboratoryAssistant(UUID id, RegisterLabAssistantRequest registerLabAssistantRequest) {
        LaboratoryAssistant labAssistant = laboratoryAssistantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory Assistant not found"));

        // Update the fields
        labAssistant.setEmail(registerLabAssistantRequest.getEmail());
        labAssistant.setName(registerLabAssistantRequest.getName());
        labAssistant.setSurname(registerLabAssistantRequest.getSurname());

        return laboratoryAssistantMapper.toDto(laboratoryAssistantRepository.save(labAssistant));
    }

    public void deleteLaboratoryAssistant(UUID id) {
        LaboratoryAssistant labAssistant = laboratoryAssistantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory Assistant not found"));
        laboratoryAssistantRepository.delete(labAssistant);
    }


}