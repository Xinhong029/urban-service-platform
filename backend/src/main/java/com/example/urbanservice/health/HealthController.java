package com.example.urbanservice.health;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides lightweight health check endpoints for the backend service.
 */
@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a health controller with access to the configured database connection.
     *
     * @param jdbcTemplate Spring helper used to run simple SQL queries
     */
    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns a basic application health response without querying the database.
     *
     * @return response containing the application status
     */
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    /**
     * Verifies database connectivity by counting imported 311 service requests.
     *
     * @return response containing the database status and service request row count
     */
    @GetMapping("/api/health/db")
    public Map<String, Object> databaseHealth() {
        Integer serviceRequestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM service_requests",
            Integer.class
        );

        return Map.of(
            "status", "ok",
            "serviceRequestCount", serviceRequestCount
        );
    }
}
