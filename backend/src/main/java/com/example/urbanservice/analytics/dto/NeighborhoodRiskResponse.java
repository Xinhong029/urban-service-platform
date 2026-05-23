package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for an operational risk score by neighborhood.
 *
 * @param neighborhood name of the analysis neighborhood
 * @param requestCount number of requests in the neighborhood
 * @param averageResolutionHours average resolution time in hours
 * @param riskScore simple 0-100 operational risk score
 */
public record NeighborhoodRiskResponse(
    String neighborhood,
    long requestCount,
    double averageResolutionHours,
    double riskScore
) {
}
