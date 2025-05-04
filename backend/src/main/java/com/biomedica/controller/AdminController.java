package com.biomedica.controller;

import com.biomedica.dto.LaboratoryAdminDto;
import com.biomedica.dto.LaboratoryAssistantDto;
import com.biomedica.dto.LaboratoryPendingTestsDto;
import com.biomedica.dto.LaboratoryRequest;
import com.biomedica.dto.MonthlyOrderStats;
import com.biomedica.dto.RegisterLabAssistantRequest;
import com.biomedica.dto.TestDto;
import com.biomedica.dto.TestRequest;
import com.biomedica.dto.TestResultDto;
import com.biomedica.dto.validation.PatchValidation;
import com.biomedica.dto.validation.PostValidation;
import com.biomedica.service.AdminService;
import com.biomedica.service.LaboratoryAssistantService;
import com.biomedica.service.LaboratoryService;
import com.biomedica.service.OrderAnalyticsService;
import com.biomedica.service.TestDataGeneratorService;
import com.biomedica.service.TestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private final LaboratoryAssistantService laboratoryAssistantService;
    private final TestDataGeneratorService testDataGeneratorService;
    private final OrderAnalyticsService orderAnalyticsService;

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

    @PostMapping("/upload-csv/laboratories")
    public ResponseEntity<?> uploadLaboratoriesFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            if (!file.getContentType().equals("text/csv") &&
                    !file.getContentType().equals("application/vnd.ms-excel")) {
                return ResponseEntity.badRequest().body("Please upload a valid CSV file");
            }

            int count = laboratoryService.processCSVFile(file);
            return ResponseEntity.ok("Successfully imported " + count + " laboratories");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/upload-csv/laboratory-assistants")
    public ResponseEntity<?> uploadLaboratoryAssistantsFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            if (!file.getContentType().equals("text/csv") &&
                    !file.getContentType().equals("application/vnd.ms-excel")) {
                return ResponseEntity.badRequest().body("Please upload a valid CSV file");
            }

            int count = laboratoryAssistantService.processCSVFile(file);
            return ResponseEntity.ok("Successfully imported " + count + " laboratory assistants");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/upload-csv/tests")
    public ResponseEntity<?> uploadTestsFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a CSV file");
            }

            if (!file.getContentType().equals("text/csv") &&
                    !file.getContentType().equals("application/vnd.ms-excel")) {
                return ResponseEntity.badRequest().body("Please upload a valid CSV file");
            }

            int count = testService.processCSVFile(file);
            return ResponseEntity.ok("Successfully imported " + count + " tests");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload CSV file: " + e.getMessage());
        }
    }

    /**
     * Endpoint to generate random test data with patients, orders, and test results.
     * Uses existing laboratories, laboratory assistants, and tests to create the data.
     *
     * @param count The number of patients to create
     * @return A ResponseEntity indicating success or failure
     */
    @PostMapping("/generate-test-data")
    public ResponseEntity<?> generateTestData(@RequestParam("count") int count) {
        try {
            if (count <= 0) {
                return ResponseEntity.badRequest().body("Count must be greater than 0");
            }

            int generatedCount = testDataGeneratorService.generateRandomData(count);
            return ResponseEntity.ok("Successfully generated " + generatedCount + " patients with test data");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate test data: " + e.getMessage());
        }
    }

    /**
     * Get aggregated order data by month for a specific year
     *
     * @param year The year to get data for
     * @return Monthly statistics including order count and total amount
     */
    @GetMapping("/analytics/orders/{year}")
    public ResponseEntity<List<MonthlyOrderStats>> getMonthlyOrderStats(@PathVariable int year) {
        if (year < 2000 || year > 2100) {
            return ResponseEntity.badRequest().build();
        }

        List<MonthlyOrderStats> stats = orderAnalyticsService.getMonthlyOrderStats(year);
        return ResponseEntity.ok(stats);
    }

}