package com.example.urbanservice.analytics;

import java.util.List;

import com.example.urbanservice.analytics.dto.AverageResolutionTimeResponse;
import com.example.urbanservice.analytics.dto.MapPointResponse;
import com.example.urbanservice.analytics.dto.MonthlyForecastResponse;
import com.example.urbanservice.analytics.dto.MonthlyRequestCountResponse;
import com.example.urbanservice.analytics.dto.NeighborhoodCountResponse;
import com.example.urbanservice.analytics.dto.NeighborhoodRiskResponse;
import com.example.urbanservice.analytics.dto.NeighborhoodRiskSimulationResponse;
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
     * Forecasts the next monthly request count using recent average month-to-month change.
     *
     * @param periods number of recent monthly periods to use
     * @return simple monthly request forecast
     */
    public MonthlyForecastResponse getMonthlyForecast(int periods) {
        int safePeriods = Math.max(3, Math.min(periods, 12));

        String sql = """
            WITH monthly_counts AS (
                SELECT request_year,
                       request_month,
                       COUNT(*) AS request_count
                FROM service_requests
                GROUP BY request_year, request_month
            ),
            recent_months AS (
                SELECT request_year,
                       request_month,
                       request_count,
                       ROW_NUMBER() OVER (ORDER BY request_year DESC, request_month DESC) AS recent_rank
                FROM monthly_counts
            ),
            ordered_recent_months AS (
                SELECT request_year,
                       request_month,
                       request_count,
                       LAG(request_count) OVER (ORDER BY request_year, request_month) AS previous_request_count
                FROM recent_months
                WHERE recent_rank <= ?
            ),
            forecast_inputs AS (
                SELECT request_year,
                       request_month,
                       request_count,
                       AVG(request_count - previous_request_count)
                           FILTER (WHERE previous_request_count IS NOT NULL) OVER () AS average_monthly_change,
                       COUNT(*) OVER () AS periods_used
                FROM ordered_recent_months
            )
            SELECT request_year,
                   request_month,
                   request_count,
                   COALESCE(average_monthly_change, 0) AS average_monthly_change,
                   GREATEST(0, ROUND(request_count + COALESCE(average_monthly_change, 0))) AS forecast_request_count,
                   periods_used
            FROM forecast_inputs
            ORDER BY request_year DESC, request_month DESC
            LIMIT 1
            """;

        return jdbcTemplate.queryForObject(
            sql,
            (resultSet, rowNumber) -> new MonthlyForecastResponse(
                resultSet.getInt("request_year"),
                resultSet.getInt("request_month"),
                resultSet.getLong("request_count"),
                Math.round(resultSet.getDouble("average_monthly_change") * 100.0) / 100.0,
                resultSet.getLong("forecast_request_count"),
                resultSet.getInt("periods_used"),
                "Recent average month-to-month change"
            ),
            safePeriods
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

    /**
     * Calculates a simple neighborhood risk score from request volume and resolution time.
     *
     * @return top neighborhood risk scores ordered from highest to lowest risk
     */
    public List<NeighborhoodRiskResponse> getNeighborhoodRisk() {
        String sql = """
            WITH neighborhood_metrics AS (
                SELECT analysis_neighborhood,
                       COUNT(*) AS request_count,
                       COALESCE(AVG(resolution_hours), 0) AS average_resolution_hours
                FROM service_requests
                GROUP BY analysis_neighborhood
            ),
            normalized_metrics AS (
                SELECT analysis_neighborhood,
                       request_count,
                       average_resolution_hours,
                       MAX(request_count) OVER () AS max_request_count,
                       MAX(average_resolution_hours) OVER () AS max_average_resolution_hours
                FROM neighborhood_metrics
            )
            SELECT analysis_neighborhood,
                   request_count,
                   average_resolution_hours,
                   ROUND(
                       (
                           (request_count::numeric / NULLIF(max_request_count, 0)) * 60
                           +
                           (average_resolution_hours::numeric / NULLIF(max_average_resolution_hours, 0)) * 40
                       )::numeric,
                       2
                   ) AS risk_score
            FROM normalized_metrics
            ORDER BY risk_score DESC
            LIMIT 10
            """;

        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new NeighborhoodRiskResponse(
                resultSet.getString("analysis_neighborhood"),
                resultSet.getLong("request_count"),
                Math.round(resultSet.getDouble("average_resolution_hours") * 100.0) / 100.0,
                resultSet.getDouble("risk_score")
            )
        );
    }

    /**
     * Simulates risk scores when one target neighborhood changes by a percentage.
     *
     * @param neighborhood neighborhood whose request volume should change
     * @param growthPercent percentage applied to the target neighborhood request count
     * @return simulated risk scores ordered from highest to lowest simulated risk
     */
    public List<NeighborhoodRiskSimulationResponse> simulateNeighborhoodRisk(
        String neighborhood,
        double growthPercent
    ) {
        double safeGrowthPercent = Math.max(-90.0, Math.min(growthPercent, 200.0));
        double growthMultiplier = 1 + (safeGrowthPercent / 100.0);

        String sql = """
            WITH neighborhood_metrics AS (
                SELECT analysis_neighborhood,
                       COUNT(*) AS request_count,
                       COALESCE(AVG(resolution_hours), 0) AS average_resolution_hours
                FROM service_requests
                GROUP BY analysis_neighborhood
            ),
            simulated_metrics AS (
                SELECT analysis_neighborhood,
                       request_count,
                       CASE
                           WHEN analysis_neighborhood = ? THEN ROUND(request_count * ?::numeric)
                           ELSE request_count
                       END AS simulated_request_count,
                       average_resolution_hours
                FROM neighborhood_metrics
            ),
            normalized_metrics AS (
                SELECT analysis_neighborhood,
                       request_count,
                       simulated_request_count,
                       average_resolution_hours,
                       MAX(request_count) OVER () AS max_request_count,
                       MAX(simulated_request_count) OVER () AS max_simulated_request_count,
                       MAX(average_resolution_hours) OVER () AS max_average_resolution_hours
                FROM simulated_metrics
            )
            SELECT analysis_neighborhood,
                   request_count,
                   simulated_request_count,
                   average_resolution_hours,
                   ROUND(
                       (
                           (request_count::numeric / NULLIF(max_request_count, 0)) * 60
                           +
                           (average_resolution_hours::numeric / NULLIF(max_average_resolution_hours, 0)) * 40
                       )::numeric,
                       2
                   ) AS current_risk_score,
                   ROUND(
                       (
                           (simulated_request_count::numeric / NULLIF(max_simulated_request_count, 0)) * 60
                           +
                           (average_resolution_hours::numeric / NULLIF(max_average_resolution_hours, 0)) * 40
                       )::numeric,
                       2
                   ) AS simulated_risk_score
            FROM normalized_metrics
            ORDER BY
                CASE WHEN analysis_neighborhood = ? THEN 0 ELSE 1 END,
                simulated_risk_score DESC
            LIMIT 10
            """;

        return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new NeighborhoodRiskSimulationResponse(
                resultSet.getString("analysis_neighborhood"),
                resultSet.getLong("request_count"),
                resultSet.getLong("simulated_request_count"),
                Math.round(resultSet.getDouble("average_resolution_hours") * 100.0) / 100.0,
                resultSet.getDouble("current_risk_score"),
                resultSet.getDouble("simulated_risk_score")
            ),
            neighborhood,
            growthMultiplier,
            neighborhood
        );
    }
}
