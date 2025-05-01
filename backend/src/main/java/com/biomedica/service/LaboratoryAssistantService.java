package com.biomedica.service;

import com.biomedica.dto.LaboratoryAssistantCSVDto;
import com.biomedica.dto.TestResultDto;
import com.biomedica.dto.mapper.TestResultMapper;
import com.biomedica.entity.Laboratory;
import com.biomedica.entity.TestResult;
import com.biomedica.entity.user.LaboratoryAssistant;
import com.biomedica.repository.LaboratoryAssistantRepository;
import com.biomedica.repository.LaboratoryRepository;
import com.biomedica.repository.TestResultRepository;
import com.biomedica.repository.UserRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LaboratoryAssistantService {

    private final TestResultRepository testResultRepository;
    private final LaboratoryAssistantRepository laboratoryAssistantRepository;
    private final TestResultMapper testResultMapper;
    private final AuditService auditService;
    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

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

    @Transactional
    public int processCSVFile(MultipartFile file) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<LaboratoryAssistantCSVDto> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(LaboratoryAssistantCSVDto.class);

            CsvToBean<LaboratoryAssistantCSVDto> csvToBean = new CsvToBeanBuilder<LaboratoryAssistantCSVDto>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<LaboratoryAssistantCSVDto> csvRecords = csvToBean.parse();

            // Get all laboratories for random assignment
            List<Laboratory> allLaboratories = laboratoryRepository.findAll();
            if (allLaboratories.isEmpty()) {
                throw new RuntimeException("No laboratories found in the database. Please create laboratories first.");
            }

            List<LaboratoryAssistant> assistants = new ArrayList<>();

            for (LaboratoryAssistantCSVDto dto : csvRecords) {
                // Check if email already exists
                if (userRepository.existsByEmail(dto.getEmail())) {
                    throw new IllegalArgumentException("Email already in use");
                }

                // Hash the password
                String hashedPassword = passwordEncoder.encode(dto.getPassword());

                // Assign a random laboratory
                Laboratory randomLab = allLaboratories.get(random.nextInt(allLaboratories.size()));

                // Create a new LabAssistant entity
                LaboratoryAssistant labAssistant = new LaboratoryAssistant();
                labAssistant.setEmail(dto.getEmail());
                labAssistant.setPhoneNumber(dto.getPhoneNumber());
                labAssistant.setPassword(hashedPassword);
                labAssistant.setName(dto.getName());
                labAssistant.setSurname(dto.getSurname());
                labAssistant.setVerified(true);
                labAssistant.setLaboratory(randomLab);

                assistants.add(labAssistant);
            }

            laboratoryAssistantRepository.saveAll(assistants);
            log.info("Successfully imported {} laboratory assistants from CSV", assistants.size());
            return assistants.size();
        } catch (Exception e) {
            log.error("Failed to parse CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }
}