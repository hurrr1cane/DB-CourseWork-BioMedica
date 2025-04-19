package com.biomedica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LaboratoryAssistantDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UUID laboratoryId;
}