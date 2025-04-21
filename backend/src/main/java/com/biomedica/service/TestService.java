package com.biomedica.service;

import com.biomedica.dto.TestDto;
import com.biomedica.dto.TestRequest;
import com.biomedica.dto.mapper.TestMapper;
import com.biomedica.entity.Test;
import com.biomedica.repository.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final TestMapper testMapper;

    public Page<TestDto> getTests(Pageable pageable) {
        return testRepository.findAll(pageable).map(testMapper::toDto);
    }

    public TestDto getTest(UUID id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with ID: " + id));
        return testMapper.toDto(test);
    }

    public TestDto createTest(TestRequest testRequest) {
        Test test = Test.builder()
                .name(testRequest.getName())
                .description(testRequest.getDescription())
                .price(testRequest.getPrice())
                .build();
        return testMapper.toDto(testRepository.save(test));
    }

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

}
