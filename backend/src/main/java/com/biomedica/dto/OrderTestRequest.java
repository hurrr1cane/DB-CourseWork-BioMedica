package com.biomedica.dto;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class OrderTestRequest {

    @Future
    private OffsetDateTime testDate;
    private UUID testId;
    private UUID laboratoryId;
}
