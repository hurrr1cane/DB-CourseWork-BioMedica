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
     * Endpoint to register a new laboratory assistant.
     * @param registerLabAssistantRequest The request body containing the details of the laboratory assistant to be registered.
     * @return A ResponseEntity containing the registered laboratory assistant's details.
     */
    @PostMapping("/laboratory-assistants")
    public ResponseEntity<LaboratoryAssistantDto> registerLabAssistant(@RequestBody @Valid RegisterLabAssistantRequest registerLabAssistantRequest) {
        return ResponseEntity.ok(adminService.registerLabAssistant(registerLabAssistantRequest));
    }

    @GetMapping("/laboratory-assistants")
    public ResponseEntity<Page<LaboratoryAssistantDto>> getLaboratoryAssistants(Pageable pageable) {
        return ResponseEntity.ok(adminService.getLaboratoryAssistants(pageable));
    }

    @GetMapping("/laboratory-assistants/{laboratoryAssistantId}")
    public ResponseEntity<LaboratoryAssistantDto> getLaboratoryAssistantById(@PathVariable UUID laboratoryAssistantId) {
        return ResponseEntity.ok(adminService.getLaboratoryAssistantById(laboratoryAssistantId));
    }

    @PutMapping("/laboratory-assistants/{laboratoryAssistantId}")
    public ResponseEntity<LaboratoryAssistantDto> editLaboratoryAssistant(
            @PathVariable UUID laboratoryAssistantId,
            @RequestBody @Validated(PatchValidation.class) RegisterLabAssistantRequest registerLabAssistantRequest
    ) {
        return ResponseEntity.ok(adminService.updateLaboratoryAssistant(laboratoryAssistantId, registerLabAssistantRequest));
    }

    @DeleteMapping("/laboratory-assistants/{laboratoryAssistantId}")
    public ResponseEntity<Void> deleteLaboratoryAssistant(@PathVariable UUID laboratoryAssistantId) {
        adminService.deleteLaboratoryAssistant(laboratoryAssistantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/laboratories")
    public ResponseEntity<Page<LaboratoryAdminDto>> getLaboratories(Pageable pageable) {
        return ResponseEntity.ok(laboratoryService.getAdminLaboratories(pageable));
    }

    @GetMapping("/laboratories/{laboratoryId}")
    public ResponseEntity<LaboratoryAdminDto> getLaboratoryById(@PathVariable UUID laboratoryId) {
        return ResponseEntity.ok(laboratoryService.getAdminLaboratoryById(laboratoryId));
    }

    @PostMapping("/laboratories")
    public ResponseEntity<LaboratoryAdminDto> createLaboratory(
            @RequestBody @Validated(PostValidation.class) LaboratoryRequest laboratoryRequest
            ) {
        return ResponseEntity.ok(laboratoryService.createLaboratory(laboratoryRequest));
    }

    @PatchMapping("/laboratories/{laboratoryId}")
    public ResponseEntity<LaboratoryAdminDto> editLaboratory(
            @PathVariable UUID laboratoryId,
            @RequestBody @Validated(PatchValidation.class) LaboratoryRequest laboratoryRequest
    ) {
        return ResponseEntity.ok(laboratoryService.editLaboratory(laboratoryId, laboratoryRequest));
    }

    @DeleteMapping("/laboratories/{laboratoryId}")
    public ResponseEntity<Void> deleteLaboratory(@PathVariable UUID laboratoryId) {
        laboratoryService.deleteLaboratory(laboratoryId);
        return ResponseEntity.noContent().build();
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

    @DeleteMapping("/tests/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable UUID testId) {
        testService.deleteTest(testId);
        return ResponseEntity.noContent().build();
    }

}