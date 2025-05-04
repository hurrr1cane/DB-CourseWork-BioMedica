package com.biomedica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyOrderStats {
    private int month;
    private int orderCount;
    private BigDecimal totalAmount;
}