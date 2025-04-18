package com.biomedica.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LaboratoryDto {
    private UUID id;
    private String address;
    private String workingHours;
    private String phoneNumber;
    private List<TestDto> tests;

}
