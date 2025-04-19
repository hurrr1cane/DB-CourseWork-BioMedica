package com.biomedica.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "administrator")
public class Administrator extends User {
    public Administrator() {
        super.setRole(Role.ADMINISTRATOR);
    }
}
