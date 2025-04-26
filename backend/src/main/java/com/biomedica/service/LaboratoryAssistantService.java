package com.biomedica.service;

import com.biomedica.dto.TestResultDto;
import com.biomedica.dto.mapper.TestResultMapper;
import com.biomedica.entity.TestResult;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.repository.LaboratoryAssistantRepository;
import com.biomedica.repository.TestResultRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LaboratoryAssistantService {

    private final TestResultRepository testResultRepository;
    private final LaboratoryAssistantRepository laboratoryAssistantRepository;
    private final TestResultMapper testResultMapper;
    private final AuditService auditService;

    /**
     * Retrieves all test results assigned to the laboratory assistant.
     *
     * @param pageable The pagination information.
     */
    @Transactional(readOnly = true)
    public Page<TestResultDto> getTestResultsForAssistant(Pageable pageable) {

        LaboratoryAssistant user = (LaboratoryAssistant) auditService.getPrincipal();
        Page<TestResult> testResults = testResultRepository.findByLaboratoryAssistant(user, pageable);
        return testResults.map(testResultMapper::toDto);
    }

    /**
     * Retrieves a specific test result assigned to the laboratory assistant.
     *
     * @param testResultId The ID of the test result.
     * @return The test result DTO.
     */
    @Transactional(readOnly = true)
    public TestResultDto getTestResultById(UUID testResultId) {
        LaboratoryAssistant user = (LaboratoryAssistant) auditService.getPrincipal();
        TestResult testResult = testResultRepository.findByIdAndLaboratoryAssistant(testResultId, user)
                .orElseThrow(() -> new EntityNotFoundException("Test result not found"));
        return testResultMapper.toDto(testResult);
    }

    /**
     * Updates a test result with the provided data.
     *
     * @param testResultId The ID of the test result to update.
     * @param result The result data to fill in.
     * @return The updated test result DTO.
     */
    @Transactional
    public TestResultDto fillTestResult(UUID testResultId, String result) {
        TestResult testResult = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new EntityNotFoundException("Test result not found"));

        LaboratoryAssistant user = (LaboratoryAssistant) auditService.getPrincipal();
        if (!testResult.getLaboratoryAssistant().getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to fill this test result");
        }

        testResult.setResult(result);

        TestResult updatedTestResult = testResultRepository.save(testResult);
        return testResultMapper.toDto(updatedTestResult);
    }

    /**
     * Cancels the assistant's participation in a test result and assigns a new assistant if available.
     *
     * @param testResultId The ID of the test result.
     */
    @Transactional
    public void cancelParticipation(UUID testResultId) {
        TestResult testResult = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new EntityNotFoundException("Test result not found"));

        LaboratoryAssistant currentAssistant = testResult.getLaboratoryAssistant();
        if (currentAssistant == null) {
            throw new IllegalStateException("No assistant is currently assigned to this test result");
        }

        LaboratoryAssistant user = (LaboratoryAssistant) auditService.getPrincipal();
        if (!currentAssistant.getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to cancel this test result");
        }

        if (testResult.getTestDate().
        isBefore(OffsetDateTime.now().plusMinutes(15))) {
            throw new IllegalStateException("You cannot cancel a test result less than 15 minutes before the test date");
        }

        // Find a new available assistant
        LaboratoryAssistant newAssistant = laboratoryAssistantRepository.findByLaboratory(currentAssistant.getLaboratory())
                .stream()
                .filter(assistant -> testResultRepository.findByLaboratoryAssistantAndTestDate(assistant, testResult.getTestDate()).isEmpty())
                .filter(assistant -> !assistant.getId().equals(currentAssistant.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available laboratory assistants at the requested time"));

        // Assign the new assistant
        testResult.setLaboratoryAssistant(newAssistant);
        testResultRepository.save(testResult);
    }
}