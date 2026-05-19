package com.example.urbanservice.analytics.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for a 311 request location shown on a map.
 *
 * @param serviceRequestId unique 311 service request identifier
 * @param serviceName name of the 311 service request category
 * @param status current request status
 * @param analysisNeighborhood neighborhood associated with the request
 * @param requestedDatetime time when the request was created
 * @param lat request latitude
 * @param lng request longitude
 */
public record MapPointResponse(
    String serviceRequestId,
    String serviceName,
    String status,
    String analysisNeighborhood,
    LocalDateTime requestedDatetime,
    double lat,
    double lng
) {
}
