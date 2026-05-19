package com.example.urbanservice.analytics.dto;

/**
 * Response DTO for a service type and its total number of 311 requests.
 *
 * @param serviceName name of the 311 service request category
 * @param requestCount number of requests in that category
 */
public record ServiceTypeCountResponse(
    String serviceName,
    long requestCount
) {
}

// record语法，定义一个不可变的数据对象
// 定义API返回的数据类型