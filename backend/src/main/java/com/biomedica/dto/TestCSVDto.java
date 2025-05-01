package com.biomedica.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCSVDto {

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvBindByName(column = "description", required = true)
    private String description;

    @CsvBindByName(column = "price", required = true)
    private String price; // String to handle potential parsing issues
}