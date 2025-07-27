package co.onmind.microhex.infrastructure.controllers;

import java.time.LocalDateTime;

/**
 * Standardized error response record for REST API endpoints.
 * 
 * This record provides a consistent structure for error responses across
 * all REST endpoints in the application. It includes essential information
 * for debugging and user feedback.
 * 
 * @param code The error code identifying the type of error
 * @param message A human-readable description of the error
 * @param timestamp The timestamp when the error occurred
 * @param path The request path where the error occurred
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp,
    String path
) {
    
    /**
     * Creates an ErrorResponse with the current timestamp.
     * 
     * @param code The error code
     * @param message The error message
     * @param path The request path
     * @return A new ErrorResponse instance
     */
    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, LocalDateTime.now(), path);
    }
    
    /**
     * Creates an ErrorResponse for validation errors.
     * 
     * @param message The validation error message
     * @param path The request path
     * @return A new ErrorResponse instance with validation error code
     */
    public static ErrorResponse validationError(String message, String path) {
        return of("VALIDATION_ERROR", message, path);
    }
    
    /**
     * Creates an ErrorResponse for not found errors.
     * 
     * @param message The not found error message
     * @param path The request path
     * @return A new ErrorResponse instance with not found error code
     */
    public static ErrorResponse notFound(String message, String path) {
        return of("NOT_FOUND", message, path);
    }
    
    /**
     * Creates an ErrorResponse for conflict errors.
     * 
     * @param message The conflict error message
     * @param path The request path
     * @return A new ErrorResponse instance with conflict error code
     */
    public static ErrorResponse conflict(String message, String path) {
        return of("CONFLICT", message, path);
    }
    
    /**
     * Creates an ErrorResponse for internal server errors.
     * 
     * @param message The internal error message
     * @param path The request path
     * @return A new ErrorResponse instance with internal error code
     */
    public static ErrorResponse internalError(String message, String path) {
        return of("INTERNAL_ERROR", message, path);
    }
}