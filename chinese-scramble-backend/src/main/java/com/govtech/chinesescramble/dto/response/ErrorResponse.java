package com.govtech.chinesescramble.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response DTO
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, Object> details
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            error,
            message,
            path,
            null
        );
    }

    public static ErrorResponse of(int status, String error, String message, String path,
                                   Map<String, Object> details) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            error,
            message,
            path,
            details
        );
    }
}
