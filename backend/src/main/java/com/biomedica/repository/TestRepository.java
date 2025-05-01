package com.biomedica.repository;

import com.biomedica.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestRepository extends JpaRepository<Test, UUID> {

    Optional<Test> findByName(String name);

}
