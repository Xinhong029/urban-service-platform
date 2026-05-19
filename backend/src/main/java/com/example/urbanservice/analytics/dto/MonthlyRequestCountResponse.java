package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for monthly 311 request volume.
 *
 * @param year request year
 * @param month request month from 1 to 12
 * @param requestCount number of requests in that month
 */
public record MonthlyRequestCountResponse(
    int year,
    int month,
    long requestCount
) {
}
