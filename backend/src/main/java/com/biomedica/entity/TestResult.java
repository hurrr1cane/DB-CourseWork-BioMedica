package com.biomedica.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OffsetDateTime testDate;

    private String result;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Test test;

    @ManyToOne
    private Laboratory laboratory;
}
