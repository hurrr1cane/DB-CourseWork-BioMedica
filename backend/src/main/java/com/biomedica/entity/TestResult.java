package com.biomedica.entity;

import com.biomedica.entity.user.LaboratoryAssistant;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
@Table(name="orders", indexes = {
        @Index(name = "idx_order_patient", columnList = "patient_id")
})
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OffsetDateTime testDate;

    private String result;

    @ManyToOne
    private Order order;

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Laboratory laboratory;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne
    @JoinColumn(name = "laboratory_assistant_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private LaboratoryAssistant laboratoryAssistant;
}
