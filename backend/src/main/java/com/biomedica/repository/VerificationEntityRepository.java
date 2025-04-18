package com.biomedica.repository;

import com.biomedica.entity.user.VerificationEntity;
import com.biomedica.entity.user.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerificationEntityRepository extends JpaRepository<VerificationEntity, UUID> {

    Optional<VerificationEntity> findByUserEmailAndVerificationType(String email, VerificationType verificationType);


    void deleteByExpirationDateBefore(OffsetDateTime date);
}
