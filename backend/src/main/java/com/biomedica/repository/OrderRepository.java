package com.biomedica.repository;

import com.biomedica.entity.Order;
import com.biomedica.entity.user.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByPatient(Patient patient, Pageable pageable);
}
