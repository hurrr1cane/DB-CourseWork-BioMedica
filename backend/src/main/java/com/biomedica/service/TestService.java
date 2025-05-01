package com.biomedica.service;

import com.biomedica.dto.TestCSVDto;
import com.biomedica.dto.TestDto;
import com.biomedica.dto.TestRequest;
import com.biomedica.dto.mapper.TestMapper;
import com.biomedica.entity.Laboratory;
import com.biomedica.entity.Test;
import com.biomedica.repository.LaboratoryRepository;
import com.biomedica.repository.TestRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final TestRepository testRepository;
    private final TestMapper testMapper;
    private final LaboratoryRepository laboratoryRepository;
    private final Random random = new Random();

    public Page<TestDto> getTests(Pageable pageable) {
        return testRepository.findAll(pageable).map(testMapper::toDto);
    }

    public TestDto getTest(UUID id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with ID: " + id));
        return testMapper.toDto(test);
    }

    @Transactional
    public TestDto createTest(TestRequest testRequest) {
        Test test = Test.builder()
                .name(testRequest.getName())
                .description(testRequest.getDescription())
                .price(testRequest.getPrice())
                .build();
        return testMapper.toDto(testRepository.save(test));
    }

    @Transactional
    public TestDto updateTest(UUID id, TestRequest testRequest) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with ID: " + id));

        if (testRequest.getName() != null) {
            test.setName(testRequest.getName());
        }
        if (testRequest.getDescription() != null) {
            test.setDescription(testRequest.getDescription());
        }
        if (testRequest.getPrice() != null) {
            test.setPrice(testRequest.getPrice());
        }

        return testMapper.toDto(testRepository.save(test));
    }

    @Transactional
    public void deleteTest(UUID testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + testId));

        // The cascade settings will handle:
        // - Removing entries from test_laboratory join table
        // - Test results will keep the test as they need historical record
        testRepository.delete(test);
    }

    @Transactional
    public int processCSVFile(MultipartFile file) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<TestCSVDto> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(TestCSVDto.class);

            CsvToBean<TestCSVDto> csvToBean = new CsvToBeanBuilder<TestCSVDto>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<TestCSVDto> csvRecords = csvToBean.parse();

            // Get all laboratories for random assignment
            List<Laboratory> allLaboratories = laboratoryRepository.findAll();
            if (allLaboratories.isEmpty()) {
                throw new RuntimeException("No laboratories found in the database. Please create laboratories first.");
            }

            List<Test> tests = new ArrayList<>();

            for (TestCSVDto dto : csvRecords) {
                // Check if test name already exists
                if (testRepository.findByName(dto.getName()).isPresent()) {
                    log.warn("Skipping test with name {} as it already exists", dto.getName());
                    continue;
                }

                // Parse price (handle possible format issues)
                double price;
                try {
                    price = Double.parseDouble(dto.getPrice().replaceAll("[^\\d.]", ""));
                } catch (NumberFormatException e) {
                    log.warn("Invalid price format for test {}: {}", dto.getName(), dto.getPrice());
                    price = 0.0; // Default price
                }

                // Assign random laboratories (between 1 and 10)
                int numLabs = random.nextInt(10) + 1;
                List<Laboratory> randomLabs = new ArrayList<>();
                for (int i = 0; i < numLabs; i++) {
                    Laboratory lab = allLaboratories.get(random.nextInt(allLaboratories.size()));
                    if (!randomLabs.contains(lab)) {
                        randomLabs.add(lab);
                    }
                }

                Test test = Test.builder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .price(price)
                        .laboratories(randomLabs)
                        .build();

                tests.add(test);
            }

            testRepository.saveAll(tests);
            log.info("Successfully imported {} tests from CSV", tests.size());
            return tests.size();
        } catch (Exception e) {
            log.error("Failed to parse CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }

}
