package com.biomedica.service;

import com.biomedica.dto.LaboratoryAdminDto;
import com.biomedica.dto.LaboratoryDto;
import com.biomedica.dto.LaboratoryPendingTestsDto;
import com.biomedica.dto.LaboratoryRequest;
import com.biomedica.dto.LaboratorySearchDto;
import com.biomedica.dto.TestResultDto;
import com.biomedica.dto.mapper.LaboratoryMapper;
import com.biomedica.entity.Laboratory;
import com.biomedica.entity.Test;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.repository.LaboratoryAssistantRepository;
import com.biomedica.repository.LaboratoryRepository;
import com.biomedica.repository.TestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class LaboratoryService {
    private final LaboratoryRepository laboratoryRepository;
    private final LaboratoryAssistantRepository laboratoryAssistantRepository;
    private final TestRepository testRepository;
    private final LaboratoryMapper laboratoryMapper;
    private final EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public LaboratoryAdminDto createLaboratory(@NonNull LaboratoryRequest laboratoryRequest) {
        // Create the laboratory first with basic information
        Laboratory laboratory = Laboratory.builder()
                .address(laboratoryRequest.getAddress())
                .phoneNumber(laboratoryRequest.getPhoneNumber())
                .workingHours(laboratoryRequest.getWorkingHours())
                .tests(new ArrayList<>())
                .laboratoryAssistants(new ArrayList<>())
                .build();

        // Save the laboratory to get its ID
        laboratory = laboratoryRepository.saveAndFlush(laboratory);

        // Add tests with proper bidirectional relationship
        if (laboratoryRequest.getTestIds() != null) {
            for (UUID testId : laboratoryRequest.getTestIds()) {
                Test test = testRepository.findById(testId)
                        .orElseThrow(() -> new EntityNotFoundException("Test with id " + testId + " was not found"));
                laboratory.getTests().add(test);
                test.getLaboratories().add(laboratory);
            }
        }

        // Add laboratory assistants with proper bidirectional relationship
        if (laboratoryRequest.getLaboratoryAssistantIds() != null) {
            for (UUID assistantId : laboratoryRequest.getLaboratoryAssistantIds()) {
                LaboratoryAssistant assistant = laboratoryAssistantRepository.findById(assistantId)
                        .orElseThrow(() -> new EntityNotFoundException("Laboratory Assistant with id " + assistantId + " was not found"));
                laboratory.getLaboratoryAssistants().add(assistant);
                assistant.setLaboratory(laboratory);
            }
        }

        // Save everything again to ensure relationships are persisted
        laboratory = laboratoryRepository.saveAndFlush(laboratory);

        return laboratoryMapper.toAdminDto(laboratory);
    }

    @Transactional
    public LaboratoryAdminDto editLaboratory(UUID laboratoryId, LaboratoryRequest laboratoryRequest) {
        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory was not found"));

        if (laboratoryRequest.getAddress() != null) {
            laboratory.setAddress(laboratoryRequest.getAddress());
        }
        if (laboratoryRequest.getPhoneNumber() != null) {
            laboratory.setPhoneNumber(laboratoryRequest.getPhoneNumber());
        }
        if (laboratoryRequest.getWorkingHours() != null) {
            laboratory.setWorkingHours(laboratoryRequest.getWorkingHours());
        }

        // Handle tests relationship
        if (laboratoryRequest.getTestIds() != null) {
            // First, remove the laboratory from tests that are no longer associated
            for (Test test : new ArrayList<>(laboratory.getTests())) {
                if (!laboratoryRequest.getTestIds().contains(test.getId())) {
                    test.getLaboratories().remove(laboratory);
                    laboratory.getTests().remove(test);
                }
            }

            // Add new tests not already in the list
            for (UUID testId : laboratoryRequest.getTestIds()) {
                Test test = testRepository.findById(testId)
                        .orElseThrow(() -> new EntityNotFoundException("Test with id " + testId + " was not found"));

                if (!laboratory.getTests().contains(test)) {
                    laboratory.getTests().add(test);
                    test.getLaboratories().add(laboratory);
                }
            }
        }

        // Handle laboratory assistants relationship
        if (laboratoryRequest.getLaboratoryAssistantIds() != null) {
            // First, update assistants that are no longer associated
            for (LaboratoryAssistant assistant : new ArrayList<>(laboratory.getLaboratoryAssistants())) {
                if (!laboratoryRequest.getLaboratoryAssistantIds().contains(assistant.getId())) {
                    assistant.setLaboratory(null);
                    laboratory.getLaboratoryAssistants().remove(assistant);
                }
            }

            // Add new laboratory assistants not already in the list
            for (UUID assistantId : laboratoryRequest.getLaboratoryAssistantIds()) {
                LaboratoryAssistant assistant = laboratoryAssistantRepository.findById(assistantId)
                        .orElseThrow(() -> new EntityNotFoundException("Laboratory Assistant with id " + assistantId + " was not found"));

                if (!laboratory.getLaboratoryAssistants().contains(assistant)) {
                    laboratory.getLaboratoryAssistants().add(assistant);
                    assistant.setLaboratory(laboratory);
                    log.info("Laboratory Assistant with id {} was added to laboratory with id {}", assistantId, laboratoryId);
                }
            }
        }

        // Explicitly flush changes to ensure they're persisted
        laboratoryRepository.saveAndFlush(laboratory);

        // Refresh the entity to ensure we have the latest state
        entityManager.refresh(laboratory);

        return laboratoryMapper.toAdminDto(laboratory);
    }

    public Page<LaboratoryDto> getLaboratories(Pageable pageable) {
        return laboratoryRepository.findAll(pageable).map(laboratoryMapper::toDto);
    }

    public Page<LaboratoryAdminDto> getAdminLaboratories(Pageable pageable) {
        return laboratoryRepository.findAll(pageable).map(laboratoryMapper::toAdminDto);
    }

    public LaboratoryDto getLaboratoryById(UUID laboratoryId) {
        Laboratory laboratory = laboratoryRepository.findById(laboratoryId).orElseThrow(() -> new EntityNotFoundException("Laboratory was not found"));
        return laboratoryMapper.toDto(laboratory);
    }

    public LaboratoryAdminDto getAdminLaboratoryById(UUID laboratoryId) {
        Laboratory laboratory = laboratoryRepository.findById(laboratoryId).orElseThrow(() -> new EntityNotFoundException("Laboratory was not found"));
        return laboratoryMapper.toAdminDto(laboratory);
    }

    public Page<LaboratoryDto> getLaboratoriesWithFilters(LaboratorySearchDto searchDto, Pageable pageable) {
        // Створення специфікації для фільтрації
        Specification<Laboratory> spec = Specification.where(null);

        if (searchDto.getAddress() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("address"), "%" + searchDto.getAddress() + "%"));
        }
        if (searchDto.getWorkingHours() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("workingHours"), "%" + searchDto.getWorkingHours() + "%"));
        }
        if (searchDto.getPhoneNumber() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("phoneNumber"), "%" + searchDto.getPhoneNumber() + "%"));
        }

        Page<Laboratory> laboratories = laboratoryRepository.findAll(spec, pageable);
        return laboratories.map(laboratoryMapper::toDto);
    }


    public List<LaboratoryPendingTestsDto> getLaboratoriesWithPendingTests() {
        String query = "SELECT * FROM GetLaboratoriesWithPendingTests();";

        return jdbcTemplate.query(query, (rs, rowNum) -> {
            LaboratoryPendingTestsDto dto = new LaboratoryPendingTestsDto();
            dto.setLaboratoryId(UUID.fromString(rs.getString("laboratory_id")));
            dto.setLaboratoryAddress(rs.getString("laboratory_address"));
            dto.setPendingTestsCount(rs.getInt("pending_tests_count"));
            return dto;
        });
    }

    public List<TestResultDto> getPendingTestsByLab(UUID labId) {
        String query = "SELECT * FROM GetPendingTestsByLab(?)";

        try {
            return jdbcTemplate.query(query, new Object[]{labId}, (rs, rowNum) -> {
                TestResultDto dto = new TestResultDto();
                dto.setId(UUID.fromString(rs.getString("test_id")));
                dto.setTestDate(rs.getTimestamp("test_time").toInstant().atOffset(ZoneOffset.UTC)); // Перетворення на OffsetDateTime

                return dto;
            });
        } catch (DataAccessException e) {
            // Обробка помилок, визначених процедурою
            String message = e.getMostSpecificCause().getMessage();
            if (message.contains("does not exist")) {
                System.out.println("Error: " + message);
            } else {
                System.out.println("Unexpected error: " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    @Transactional
    public void deleteLaboratory(UUID laboratoryId) {
        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory not found with id: " + laboratoryId));

        // Clear the many-to-many relationship with tests first
        laboratory.getTests().forEach(test -> test.getLaboratories().remove(laboratory));
        laboratory.getTests().clear();

        // Now delete the laboratory
        laboratoryRepository.delete(laboratory);
    }


}
