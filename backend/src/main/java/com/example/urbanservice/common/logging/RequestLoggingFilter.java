package com.example.urbanservice.common.logging;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs each API request with method, path, response status, and duration.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * Logs timing information around each API request.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain remaining filters and controller handling
     * @throws ServletException if request handling fails
     * @throws IOException if request or response I/O fails
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;

            LOGGER.info(
                "{} {} -> {} in {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs
            );
        }
    }

    /**
     * Limits request logging to backend API routes.
     *
     * @param request current HTTP request
     * @return true when the request should not be logged
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }
}
