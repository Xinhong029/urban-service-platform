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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes analytics endpoints that supply aggregated 311 service request data.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Creates an analytics controller backed by the analytics service layer.
     *
     * @param analyticsService service used to query analytics data
     */
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Returns the most frequent 311 service request categories.
     *
     * @return top service types ordered by request count descending
     */
    @GetMapping("/top-service-types")
    public List<ServiceTypeCountResponse> getTopServiceTypes() {
        return analyticsService.getTopServiceTypes();
    }

    /**
     * Returns the neighborhoods with the highest 311 request volume.
     *
     * @return top neighborhoods ordered by request count descending
     */
    @GetMapping("/top-neighborhoods")
    public List<NeighborhoodCountResponse> getTopNeighborhoods() {
        return analyticsService.getTopNeighborhoods();
    }

    /**
     * Returns monthly 311 request volume in chronological order.
     *
     * @return monthly request counts grouped by year and month
     */
    @GetMapping("/monthly-request-counts")
    public List<MonthlyRequestCountResponse> getMonthlyRequestCounts() {
        return analyticsService.getMonthlyRequestCounts();
    }

    /**
     * Returns a simple forecast for the next monthly request count.
     *
     * @param periods number of recent monthly periods to use
     * @return monthly request forecast based on recent average change
     */
    @GetMapping("/monthly-forecast")
    public MonthlyForecastResponse getMonthlyForecast(
        @RequestParam(defaultValue = "6") int periods
    ) {
        return analyticsService.getMonthlyForecast(periods);
    }

    /**
     * Returns the average number of hours needed to resolve 311 requests.
     *
     * @return average resolution time KPI
     */
    @GetMapping("/average-resolution-time")
    public AverageResolutionTimeResponse getAverageResolutionTime() {
        return analyticsService.getAverageResolutionTime();
    }

    /**
     * Returns recent 311 request locations for map visualization.
     *
     * @param limit maximum number of map points to return
     * @return recent map points with request context and coordinates
     */
    @GetMapping("/map-points")
    public List<MapPointResponse> getMapPoints(
        @RequestParam(defaultValue = "500") int limit
    ) {
        return analyticsService.getMapPoints(limit);
    }

    /**
     * Returns neighborhoods ranked by a simple operational risk score.
     *
     * @return neighborhood risk scores ordered from highest to lowest
     */
    @GetMapping("/neighborhood-risk")
    public List<NeighborhoodRiskResponse> getNeighborhoodRisk() {
        return analyticsService.getNeighborhoodRisk();
    }

    /**
     * Simulates neighborhood risk scores after one target neighborhood changes by a percentage.
     *
     * @param neighborhood neighborhood whose request volume should change
     * @param growthPercent percentage growth applied to the target neighborhood
     * @return simulated neighborhood risk scores ordered from highest to lowest
     */
    @GetMapping("/neighborhood-risk/simulation")
    public List<NeighborhoodRiskSimulationResponse> simulateNeighborhoodRisk(
        @RequestParam(defaultValue = "Mission") String neighborhood,
        @RequestParam(defaultValue = "20") double growthPercent
    ) {
        return analyticsService.simulateNeighborhoodRisk(neighborhood, growthPercent);
    }
}
