package com.biomedica.entity.user;

import com.biomedica.entity.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "patient")
public class Patient extends User {

    public Patient() {
        super.setRole(Role.PATIENT);
    }


    @OneToMany(mappedBy = "patient")
    private List<Order> orders;
}
