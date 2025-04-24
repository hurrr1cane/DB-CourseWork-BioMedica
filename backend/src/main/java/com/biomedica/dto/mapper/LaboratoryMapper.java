package com.biomedica.dto.mapper;

import com.biomedica.dto.LaboratoryAdminDto;
import com.biomedica.dto.LaboratoryDto;
import com.biomedica.entity.Laboratory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LaboratoryMapper {

    private final TestMapper testMapper;
    private final LaboratoryAssistantMapper laboratoryAssistantMapper;

    public LaboratoryDto toDto(Laboratory laboratory) {
        return LaboratoryDto.builder()
                .id(laboratory.getId())
                .address(laboratory.getAddress())
                .workingHours(laboratory.getWorkingHours())
                .phoneNumber(laboratory.getPhoneNumber())
                .tests(testMapper.toDtoList(laboratory.getTests()))
                .build();
    }

    public LaboratoryAdminDto toAdminDto(Laboratory laboratory) {
        return LaboratoryAdminDto.builder()
                .id(laboratory.getId())
                .address(laboratory.getAddress())
                .workingHours(laboratory.getWorkingHours())
                .phoneNumber(laboratory.getPhoneNumber())
                .tests(testMapper.toDtoList(laboratory.getTests()))
                .assistants(laboratory.getLaboratoryAssistants().stream().map(laboratoryAssistantMapper::toDto).toList())
                .build();
    }
}
