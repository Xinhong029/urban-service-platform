package com.example.urbanservice.analytics;

import java.util.List;

import com.example.urbanservice.analytics.dto.AverageResolutionTimeResponse;
import com.example.urbanservice.analytics.dto.MapPointResponse;
import com.example.urbanservice.analytics.dto.MonthlyRequestCountResponse;
import com.example.urbanservice.analytics.dto.NeighborhoodCountResponse;
import com.example.urbanservice.analytics.dto.ServiceTypeCountResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Runs analytics queries against the service request database table.
 */
@Service
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates an analytics service with a JDBC query helper.
     *
     * @param jdbcTemplate Spring helper used to execute SQL queries
     */
    public AnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Queries the top 10 service request categories by request volume.
     *
     * @return service type counts ordered from highest to lowest request count
     */
    public List<ServiceTypeCountResponse> getTopServiceTypes() {
        String sql = """
            SELECT service_name, COUNT(*) AS request_count
            FROM service_requests
            GROUP BY service_name
            ORDER BY request_count DESC
            LIMIT 10
            """;

        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new ServiceTypeCountResponse(
                resultSet.getString("service_name"),
                resultSet.getLong("request_count")
            )
        );
    }

    /**
     * Queries the top 10 neighborhoods by 311 request volume.
     *
     * @return neighborhood counts ordered from highest to lowest request count
     */
    public List<NeighborhoodCountResponse> getTopNeighborhoods() {
        String sql = """
            SELECT analysis_neighborhood, COUNT(*) AS request_count
            FROM service_requests
            GROUP BY analysis_neighborhood
            ORDER BY request_count DESC
            LIMIT 10
            """;

        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new NeighborhoodCountResponse(
                resultSet.getString("analysis_neighborhood"),
                resultSet.getLong("request_count")
            )
        );
    }

    /**
     * Queries monthly 311 request volume ordered chronologically.
     *
     * @return request counts grouped by request year and month
     */
    public List<MonthlyRequestCountResponse> getMonthlyRequestCounts() {
        String sql = """
            SELECT request_year, request_month, COUNT(*) AS request_count
            FROM service_requests
            GROUP BY request_year, request_month
            ORDER BY request_year, request_month
            """;

        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new MonthlyRequestCountResponse(
                resultSet.getInt("request_year"),
                resultSet.getInt("request_month"),
                resultSet.getLong("request_count")
            )
        );
    }

    /**
     * Calculates the average time required to close 311 service requests.
     *
     * @return average resolution time in hours
     */
    public AverageResolutionTimeResponse getAverageResolutionTime() {
        String sql = """
            SELECT AVG(resolution_hours) AS average_resolution_hours
            FROM service_requests
            WHERE resolution_hours IS NOT NULL
            """;

        Double averageResolutionHours = jdbcTemplate.queryForObject(sql, Double.class);

        return new AverageResolutionTimeResponse(
            averageResolutionHours == null ? 0.0 : Math.round(averageResolutionHours * 100.0) / 100.0
        );
    }

    /**
     * Queries recent 311 request locations for map visualization.
     *
     * @param limit maximum number of map points to return
     * @return recent service request map points ordered by request time descending
     */
    public List<MapPointResponse> getMapPoints(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));

        String sql = """
            SELECT service_request_id,
                   service_name,
                   status,
                   analysis_neighborhood,
                   requested_datetime,
                   lat,
                   lng
            FROM service_requests
            ORDER BY requested_datetime DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new MapPointResponse(
                resultSet.getString("service_request_id"),
                resultSet.getString("service_name"),
                resultSet.getString("status"),
                resultSet.getString("analysis_neighborhood"),
                resultSet.getTimestamp("requested_datetime").toLocalDateTime(),
                resultSet.getDouble("lat"),
                resultSet.getDouble("lng")
            ),
            safeLimit
        );
    }
}
