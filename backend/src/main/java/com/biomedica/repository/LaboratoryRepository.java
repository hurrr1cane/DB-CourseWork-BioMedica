package com.biomedica.repository;

import com.biomedica.entity.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LaboratoryRepository extends JpaRepository<Laboratory, UUID>, JpaSpecificationExecutor<Laboratory> {
}
