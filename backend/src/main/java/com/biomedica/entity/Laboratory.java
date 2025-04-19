package com.biomedica.entity;

import com.biomedica.entity.user.LaboratoryAssistant;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class Laboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String address;

    private String workingHours;

    private String phoneNumber;

    @ManyToMany(mappedBy = "laboratories")
    private List<Test> tests;

    @OneToMany(mappedBy = "laboratory")
    private List<LaboratoryAssistant> laboratoryAssistants;

}
