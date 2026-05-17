package com.transactiongraph.dto;

import java.time.LocalDateTime;

/**
 * DTO for error responses
 * 
 * @author Pablo Guasco
 * @version 1.0.0
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now());
    }
}
