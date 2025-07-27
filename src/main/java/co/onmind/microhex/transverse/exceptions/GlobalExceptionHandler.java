package co.onmind.microhex.transverse.exceptions;

import co.onmind.microhex.domain.exceptions.DuplicateRoleException;
import co.onmind.microhex.domain.exceptions.RoleNotFoundException;
import co.onmind.microhex.infrastructure.controllers.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * 
 * This class provides centralized exception handling for all REST controllers
 * in the application. It converts domain exceptions and validation errors
 * into appropriate HTTP responses with standardized error formats.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles RoleNotFoundException and returns HTTP 404.
     * 
     * @param ex The RoleNotFoundException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with ErrorResponse and HTTP 404 status
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(
            RoleNotFoundException ex, 
            HttpServletRequest request) {
        
        logger.warn("Role not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.notFound(
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handles DuplicateRoleException and returns HTTP 409.
     * 
     * @param ex The DuplicateRoleException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with ErrorResponse and HTTP 409 status
     */
    @ExceptionHandler(DuplicateRoleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRole(
            DuplicateRoleException ex, 
            HttpServletRequest request) {
        
        logger.warn("Duplicate role: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.conflict(
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handles validation errors from @Valid annotations and returns HTTP 400.
     * 
     * @param ex The MethodArgumentNotValidException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with ErrorResponse and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        logger.warn("Validation error: {}", errorMessage);
        
        ErrorResponse errorResponse = ErrorResponse.validationError(
            errorMessage,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles constraint violation exceptions and returns HTTP 400.
     * 
     * @param ex The ConstraintViolationException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with ErrorResponse and HTTP 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, 
            HttpServletRequest request) {
        
        String errorMessage = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));
        
        logger.warn("Constraint violation: {}", errorMessage);
        
        ErrorResponse errorResponse = ErrorResponse.validationError(
            errorMessage,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles IllegalArgumentException and returns HTTP 400.
     * 
     * @param ex The IllegalArgumentException that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with ErrorResponse and HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.validationError(
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles all other unexpected exceptions and returns HTTP 500.
     * 
     * @param ex The Exception that was thrown
     * @param request The HTTP request that caused the exception
     * @return ResponseEntity with ErrorResponse and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.internalError(
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}