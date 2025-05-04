package com.biomedica.service;

import com.biomedica.dto.MonthlyOrderStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get monthly order statistics for a specific year
     *
     * @param year The year to get statistics for
     * @return List of monthly statistics containing month number, order count, and total amount
     */
    public List<MonthlyOrderStats> getMonthlyOrderStats(int year) {
        log.info("Fetching monthly order statistics for year: {}", year);

        String sql = """
            SELECT 
                EXTRACT(MONTH FROM o.order_date) as month,
                COUNT(DISTINCT o.id) as order_count,
                COALESCE(SUM(t.price), 0) as total_amount
            FROM 
                orders o
            LEFT JOIN 
                test_result tr ON o.id = tr.order_id
            LEFT JOIN 
                test t ON tr.test_id = t.id
            WHERE 
                EXTRACT(YEAR FROM o.order_date) = ?
            GROUP BY 
                EXTRACT(MONTH FROM o.order_date)
            ORDER BY 
                month
        """;

        try {
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> new MonthlyOrderStats(
                            rs.getInt("month"),
                            rs.getInt("order_count"),
                            rs.getBigDecimal("total_amount")
                    ),
                    year);
        } catch (DataAccessException e) {
            log.error("Failed to retrieve monthly order statistics for year {}", year, e);
            throw new RuntimeException("Failed to retrieve order statistics", e);
        }
    }
}