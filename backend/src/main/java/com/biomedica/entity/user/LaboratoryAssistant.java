package com.biomedica.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "laboratory_assistant")
public class LaboratoryAssistant extends User {

    private String labAssistantField1; // Example specific field
    private String labAssistantField2; // Another field

}
