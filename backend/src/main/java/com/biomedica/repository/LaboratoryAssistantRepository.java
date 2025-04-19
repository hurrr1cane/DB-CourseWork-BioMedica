package com.biomedica.repository;

import com.biomedica.entity.Laboratory;
import com.biomedica.entity.user.LaboratoryAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LaboratoryAssistantRepository extends JpaRepository<LaboratoryAssistant, UUID> {

    /**
     * Finds all laboratory assistants who work in a specific laboratory
     * @param laboratory the laboratory where assistants work
     * @return list of laboratory assistants working in the given laboratory
     */
    List<LaboratoryAssistant> findByLaboratory(Laboratory laboratory);
}