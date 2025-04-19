package com.biomedica.dto.mapper;

import com.biomedica.dto.TestResultDto;
import com.biomedica.entity.TestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TestResultMapper {
    private final TestMapper testMapper;
    private final LaboratoryMapper laboratoryMapper;
    private final LaboratoryAssistantMapper laboratoryAssistantMapper;

    public TestResultDto toDto(TestResult testResult) {
        return TestResultDto.builder()
                .id(testResult.getId())
                .result(testResult.getResult())
                .test(testMapper.toDto(testResult.getTest()))
                .laboratory(laboratoryMapper.toDto(testResult.getLaboratory()))
                .laboratoryAssistant(laboratoryAssistantMapper.toDto(testResult.getLaboratoryAssistant()))
                .testDate(testResult.getTestDate())
                .build();
    }

    public List<TestResultDto> toDtoList(List<TestResult> testResults) {
        return testResults.stream()
                .map(this::toDto)
                .toList();
    }


}
