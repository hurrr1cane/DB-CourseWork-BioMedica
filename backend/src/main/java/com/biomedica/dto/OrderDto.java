package com.biomedica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderDto {

    private UUID id;

    private OffsetDateTime orderDate;

    private boolean isPaid;


    private List<TestResultDto> testResults;
}
