package com.example.urbanservice.common.error;

import java.time.Instant;

import com.example.urbanservice.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts backend exceptions into consistent JSON API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles database access failures, such as connection or SQL query errors.
     *
     * @param exception database exception raised by Spring JDBC
     * @param request current HTTP request
     * @return standardized service unavailable error response
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(
        DataAccessException exception,
        HttpServletRequest request
    ) {
        LOGGER.error(
            "Database error while handling {} {}",
            request.getMethod(),
            request.getRequestURI(),
            exception
        );

        return buildErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Database error",
            "The service request database is currently unavailable.",
            request
        );
    }

    /**
     * Handles unexpected backend failures.
     *
     * @param exception unexpected exception
     * @param request current HTTP request
     * @return standardized internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error(
            "Unexpected error while handling {} {}",
            request.getMethod(),
            request.getRequestURI(),
            exception
        );

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected backend error occurred.",
            request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
        HttpStatus status,
        String error,
        String message,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            error,
            message,
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
