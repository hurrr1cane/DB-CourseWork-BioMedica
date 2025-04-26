package com.biomedica.repository;

import com.biomedica.entity.Laboratory;
import com.biomedica.entity.Test;
import com.biomedica.entity.TestResult;
import com.biomedica.entity.user.LaboratoryAssistant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

    List<TestResult> findByLaboratoryAndTestAndTestDate(Laboratory laboratory, Test test, OffsetDateTime testDate);

    List<TestResult> findByLaboratoryAssistantAndTestDate(LaboratoryAssistant laboratoryAssistant, OffsetDateTime testDate);

    Page<TestResult> findByLaboratoryAssistant(LaboratoryAssistant laboratoryAssistant, Pageable pageable);

    Optional<TestResult> findByIdAndLaboratoryAssistant(UUID id, LaboratoryAssistant laboratoryAssistant);
}
