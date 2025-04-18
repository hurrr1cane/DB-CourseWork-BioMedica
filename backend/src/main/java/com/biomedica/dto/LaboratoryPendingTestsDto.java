package com.biomedica.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LaboratoryPendingTestsDto {
    private UUID laboratoryId;
    private String laboratoryAddress;
    private Integer pendingTestsCount;
}
