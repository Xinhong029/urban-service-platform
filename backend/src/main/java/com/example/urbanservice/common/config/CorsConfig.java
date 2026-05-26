package com.example.urbanservice.common.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures allowed browser origins for frontend-to-backend API calls.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    /**
     * Creates CORS configuration from a comma-separated environment property.
     *
     * @param allowedOrigins comma-separated frontend origins allowed to call the API
     */
    public CorsConfig(
        @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080}")
        String allowedOrigins
    ) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toArray(String[]::new);
    }

    /**
     * Allows configured frontend origins to call backend API routes.
     *
     * @param registry Spring MVC CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET")
            .allowedHeaders("*");
    }
}
