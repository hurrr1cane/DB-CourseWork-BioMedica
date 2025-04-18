package com.biomedica.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@Schema(name = "ExceptionResponse", description = "Response with error message")
@AllArgsConstructor
@Builder
@Data
public class ExceptionResponse {
    @Schema(example = "BAD_REQUEST", description = "HTTP status code")
    HttpStatus status;

    @Schema(example = "Invalid request", description = "Error message")
    String message;

    @Schema(example = "2021-08-01T12:00:00Z", description = "Timestamp of the error")
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime timestamp = OffsetDateTime.now();
}
