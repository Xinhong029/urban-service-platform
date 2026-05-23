package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for a simple monthly request volume forecast.
 *
 * @param latestYear latest year available in the cleaned dataset
 * @param latestMonth latest month available in the cleaned dataset
 * @param latestRequestCount request count in the latest available month
 * @param averageMonthlyChange average month-to-month change across recent months
 * @param forecastRequestCount forecast request count for the next period
 * @param periodsUsed number of monthly periods used in the forecast calculation
 * @param method short description of the forecasting method
 */
public record MonthlyForecastResponse(
    int latestYear,
    int latestMonth,
    long latestRequestCount,
    double averageMonthlyChange,
    long forecastRequestCount,
    int periodsUsed,
    String method
) {
}
