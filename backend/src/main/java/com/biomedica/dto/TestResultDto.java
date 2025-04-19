package com.biomedica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TestResultDto {
    private UUID id;

    private OffsetDateTime testDate;

    private String result;

    private TestDto test;

    private LaboratoryDto laboratory;

    private LaboratoryAssistantDto laboratoryAssistant;
}
