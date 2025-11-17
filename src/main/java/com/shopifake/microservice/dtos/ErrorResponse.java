package com.shopifake.microservice.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Standard error response payload for the catalog service.
 */
@Value
@Builder
public class ErrorResponse {

    /**
     * Timestamp of the error.
     */
    LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    int status;

    /**
     * Short error label.
     */
    String error;

    /**
     * Human readable message.
     */
    String message;

    /**
     * Request path that triggered the error.
     */
    String path;
}


