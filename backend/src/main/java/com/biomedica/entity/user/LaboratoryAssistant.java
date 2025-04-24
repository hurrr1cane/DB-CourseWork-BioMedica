package com.biomedica.entity.user;

import com.biomedica.entity.Laboratory;
import com.biomedica.entity.TestResult;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@Table(name = "laboratory_assistant")
public class LaboratoryAssistant extends User {

    private String phoneNumber;

    public LaboratoryAssistant() {
        super.setRole(Role.LABORATORY_ASSISTANT);
    }

    @OneToMany(mappedBy = "laboratoryAssistant")
    private List<TestResult> testResults;

    @ManyToOne
    private Laboratory laboratory;
}
