package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for a neighborhood and its total number of 311 requests.
 *
 * @param neighborhood name of the analysis neighborhood
 * @param requestCount number of requests in that neighborhood
 */
public record NeighborhoodCountResponse(
    String neighborhood,
    long requestCount
) {
}
