package com.example.urbanservice.common.dto;

import java.time.Instant;

/**
 * Standard error response returned by the backend when an API request fails.
 *
 * @param timestamp time when the error response was created
 * @param status HTTP status code
 * @param error short error category
 * @param message human-readable explanation of the failure
 * @param path request path that caused the failure
 */
public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
