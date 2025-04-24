package com.biomedica.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LaboratoryAdminDto {
    private UUID id;
    private String address;
    private String workingHours;
    private String phoneNumber;
    private List<TestDto> tests;
    private List<LaboratoryAssistantDto> assistants;

}
