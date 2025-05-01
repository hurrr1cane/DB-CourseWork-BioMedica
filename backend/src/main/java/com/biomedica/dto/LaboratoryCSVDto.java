package com.biomedica.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryCSVDto {

    @CsvBindByName(column = "address", required = true)
    private String address;

    @CsvBindByName(column = "workingHours", required = true)
    private String workingHours;

    @CsvBindByName(column = "phoneNumber", required = true)
    private String phoneNumber;
}