package com.biomedica.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "administrator")
public class Administrator extends User {

    private String adminSpecificField; // Example specific field

    // Add more fields as required for administrator-specific information
}
