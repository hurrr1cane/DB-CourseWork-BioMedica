package com.biomedica.controller;

import com.biomedica.dto.TestResultDto;
import com.biomedica.service.LaboratoryAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/api/lab")
@Tag(name = "Laboratory Assistant API")
public class LaboratoryAssistantController {

    private final LaboratoryAssistantService laboratoryAssistantService;

    @GetMapping("/test-results")
    @Operation(summary = "Get all test results assigned to the assistant")
    public ResponseEntity<Page<TestResultDto>> getTestResults(Pageable pageable) {
        return ResponseEntity.ok(laboratoryAssistantService.getTestResultsForAssistant(pageable));
    }

    @GetMapping("/test-results/{testResultId}")
    @Operation(summary = "Get a specific test result assigned to the assistant")
    public ResponseEntity<TestResultDto> getTestResultById(@PathVariable UUID testResultId) {
        return ResponseEntity.ok(laboratoryAssistantService.getTestResultById(testResultId));
    }

    @PutMapping("/test-results/{testResultId}/fill")
    @Operation(summary = "Fill a test result with data")
    public ResponseEntity<TestResultDto> fillTestResult(
            @PathVariable UUID testResultId,
            @RequestBody String resultData) {
        return ResponseEntity.ok(laboratoryAssistantService.fillTestResult(testResultId, resultData));
    }

    @PostMapping("/test-results/{testResultId}/cancel")
    @Operation(summary = "Cancel assistant's participation in a test result")
    public ResponseEntity<Void> cancelParticipation(@PathVariable UUID testResultId) {
        laboratoryAssistantService.cancelParticipation(testResultId);
        return ResponseEntity.noContent().build();
    }
}