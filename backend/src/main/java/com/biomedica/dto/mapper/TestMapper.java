package com.biomedica.dto.mapper;

import com.biomedica.dto.TestDto;
import com.biomedica.entity.Test;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestMapper {
    public TestDto toDto(Test test) {
        return TestDto.builder()
                .id(test.getId())
                .name(test.getName())
                .description(test.getDescription())
                .price(test.getPrice())
                .build();
    }

    public List<TestDto> toDtoList(List<Test> tests) {
        return tests.stream()
                .map(this::toDto)
                .toList();
    }
}
