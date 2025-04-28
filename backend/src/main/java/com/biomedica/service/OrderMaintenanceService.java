package com.biomedica.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for maintaining orders through scheduled tasks
 */
@Service
public class OrderMaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(OrderMaintenanceService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OrderMaintenanceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Scheduled task to delete unpaid orders older than 7 days
     * Runs every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void cleanupStaleOrders() {
        logger.info("Starting cleanup of stale unpaid orders");

        try {
            // Call the PostgreSQL stored procedure
            jdbcTemplate.execute("CALL delete_stale_unpaid_orders(7)");
            logger.info("Successfully cleaned up stale unpaid orders");
        } catch (Exception e) {
            logger.error("Error while cleaning up stale unpaid orders", e);
        }
    }

    /**
     * Manual trigger for cleaning up orders with a custom threshold
     * @param daysThreshold Number of days after which unpaid orders are considered stale
     * @return Number of deleted orders
     */
    @Transactional
    public int manualCleanup(int daysThreshold) {
        logger.info("Starting manual cleanup of stale unpaid orders older than {} days", daysThreshold);

        try {
            // For manual cleanup, we might want to return the count of deleted records
            Integer deletedCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM orders WHERE is_paid = FALSE AND order_date < (CURRENT_TIMESTAMP - (? || ' days')::INTERVAL)",
                    Integer.class,
                    daysThreshold
            );

            jdbcTemplate.execute("CALL delete_stale_unpaid_orders(" + daysThreshold + ")");

            logger.info("Manual cleanup completed. Deleted {} orders", deletedCount);
            return deletedCount != null ? deletedCount : 0;
        } catch (Exception e) {
            logger.error("Error during manual cleanup", e);
            throw e;
        }
    }
}