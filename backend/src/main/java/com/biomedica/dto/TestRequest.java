package com.biomedica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TestRequest {

    private String name;

    private String description;

    private Double price;
}
