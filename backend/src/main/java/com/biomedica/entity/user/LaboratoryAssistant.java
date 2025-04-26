package com.biomedica.entity.user;

import com.biomedica.entity.Laboratory;
import com.biomedica.entity.TestResult;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@Table(name = "laboratory_assistant", indexes = {
        @Index(name = "idx_lab_assistant_laboratory", columnList = "laboratory_id")
})
public class LaboratoryAssistant extends User {

    private String phoneNumber;

    public LaboratoryAssistant() {
        super.setRole(Role.LABORATORY_ASSISTANT);
    }

    @OneToMany(mappedBy = "laboratoryAssistant")
    private List<TestResult> testResults;

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Laboratory laboratory;
}
