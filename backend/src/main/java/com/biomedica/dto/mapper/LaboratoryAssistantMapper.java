package com.biomedica.dto.mapper;

import com.biomedica.dto.LaboratoryAssistantDto;
import com.biomedica.entity.user.LaboratoryAssistant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LaboratoryAssistantMapper {

    public LaboratoryAssistantDto toDto(LaboratoryAssistant laboratoryAssistant) {
        if (laboratoryAssistant == null) {
            return null;
        }

        return LaboratoryAssistantDto.builder()
                .id(laboratoryAssistant.getId())
                .firstName(laboratoryAssistant.getName())
                .lastName(laboratoryAssistant.getSurname())
                .email(laboratoryAssistant.getEmail())
                .laboratoryId(laboratoryAssistant.getLaboratory() != null ?
                        laboratoryAssistant.getLaboratory().getId() : null)
                .build();
    }

    public List<LaboratoryAssistantDto> toDtoList(List<LaboratoryAssistant> laboratoryAssistants) {
        if (laboratoryAssistants == null) {
            return List.of();
        }

        return laboratoryAssistants.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}