package com.biomedica.controller;

import com.biomedica.dto.LaboratoryDto;
import com.biomedica.dto.LaboratorySearchDto;
import com.biomedica.service.LaboratoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/api/laboratories")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @GetMapping
    public ResponseEntity<Page<LaboratoryDto>> getLaboratories(Pageable pageable) {
        return ResponseEntity.ok(laboratoryService.getLaboratories(pageable));
    }

    @GetMapping("/{laboratoryId}")
    public ResponseEntity<LaboratoryDto> getLaboratoryById(@PathVariable UUID laboratoryId) {
        return ResponseEntity.ok(laboratoryService.getLaboratoryById(laboratoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<LaboratoryDto>> getLaboratoriesWithFilters(
            LaboratorySearchDto searchDto,
            Pageable pageable) {

        return ResponseEntity.ok(laboratoryService.getLaboratoriesWithFilters(searchDto, pageable));
    }

}
