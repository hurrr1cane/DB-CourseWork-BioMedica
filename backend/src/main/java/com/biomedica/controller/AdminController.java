package com.biomedica.controller;

import com.biomedica.dto.*;
import com.biomedica.dto.validation.PatchValidation;
import com.biomedica.dto.validation.PostValidation;
import com.biomedica.service.AdminService;
import com.biomedica.service.LaboratoryService;
import com.biomedica.service.TestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin API")
public class AdminController {

    private final AdminService adminService;
    private final LaboratoryService laboratoryService;
    private final TestService testService;

    /**
     * * Endpoint to register a new laboratory assistant.
     * @param registerLabAssistantRequest The request body containing the details of the laboratory assistant to be registered.
     * @return A ResponseEntity containing the registered laboratory assistant's details.
     */
    @PostMapping("/register-lab-assistant")
    public ResponseEntity<UserDto> registerLabAssistant(@RequestBody @Valid RegisterLabAssistantRequest registerLabAssistantRequest) {
        return ResponseEntity.ok(adminService.registerLabAssistant(registerLabAssistantRequest));
    }

    @PostMapping("/laboratories")
    public ResponseEntity<LaboratoryDto> createLaboratory(
            @RequestBody @Validated(PostValidation.class) LaboratoryRequest laboratoryRequest
            ) {
        return ResponseEntity.ok(laboratoryService.createLaboratory(laboratoryRequest));
    }

    @PatchMapping("/laboratories/{laboratoryId}")
    public ResponseEntity<LaboratoryDto> editLaboratory(
            @PathVariable UUID laboratoryId,
            @RequestBody @Validated(PatchValidation.class) LaboratoryRequest laboratoryRequest
    ) {
        return ResponseEntity.ok(laboratoryService.editLaboratory(laboratoryId, laboratoryRequest));
    }

    @GetMapping("/laboratories/pending-tests")
    public ResponseEntity<List<LaboratoryPendingTestsDto>> getLaboratoriesWithPendingTests() {
        return ResponseEntity.ok(laboratoryService.getLaboratoriesWithPendingTests());
    }

    @GetMapping("/laboratories/{laboratoryId}/pending-tests")
    public List<TestResultDto> getPendingTestsByLab(@PathVariable UUID laboratoryId) {
        return laboratoryService.getPendingTestsByLab(laboratoryId);
    }

    @GetMapping("/tests")
    public ResponseEntity<Page<TestDto>> getTests(
            Pageable pageable
    ) {
        return ResponseEntity.ok(testService.getTests(pageable));
    }

    @GetMapping("/tests/{testId}")
    public ResponseEntity<TestDto> getTestById(
            @PathVariable UUID testId
    ) {
        return ResponseEntity.ok(testService.getTest(testId));
    }

    @PostMapping("/tests")
    public ResponseEntity<TestDto> createTest(
            @RequestBody @Validated(PostValidation.class) TestRequest testRequest
    ) {
        return ResponseEntity.ok(testService.createTest(testRequest));
    }

    @PatchMapping("/tests/{testId}")
    public ResponseEntity<TestDto> updateTest(
            @PathVariable UUID testId,
            @RequestBody @Validated(PatchValidation.class) TestRequest testRequest
    ) {
        return ResponseEntity.ok(testService.updateTest(testId, testRequest));
    }

}