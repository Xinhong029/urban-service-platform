package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for a neighborhood risk simulation under request growth.
 *
 * @param neighborhood name of the analysis neighborhood
 * @param currentRequestCount current number of requests in the neighborhood
 * @param simulatedRequestCount simulated number of requests after growth is applied
 * @param averageResolutionHours average resolution time in hours
 * @param currentRiskScore current 0-100 operational risk score
 * @param simulatedRiskScore simulated 0-100 operational risk score
 */
public record NeighborhoodRiskSimulationResponse(
    String neighborhood,
    long currentRequestCount,
    long simulatedRequestCount,
    double averageResolutionHours,
    double currentRiskScore,
    double simulatedRiskScore
) {
}
