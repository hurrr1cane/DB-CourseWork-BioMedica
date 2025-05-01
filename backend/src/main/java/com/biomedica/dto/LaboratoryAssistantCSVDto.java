package com.biomedica.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryAssistantCSVDto {

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvBindByName(column = "surname", required = true)
    private String surname;

    @CsvBindByName(column = "email", required = true)
    private String email;

    @CsvBindByName(column = "phoneNumber", required = true)
    private String phoneNumber;

    @CsvBindByName(column = "password", required = true)
    private String password;
}