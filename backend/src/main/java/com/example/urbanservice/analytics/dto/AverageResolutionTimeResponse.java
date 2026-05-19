package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for average 311 service request resolution time.
 *
 * @param averageResolutionHours average number of hours between request creation and closure
 */
public record AverageResolutionTimeResponse(
    double averageResolutionHours
) {
}
