package co.onmind.microhex.domain.exceptions;

/**
 * Exception thrown when attempting to create a role that already exists.
 */
public class RoleAlreadyExistsException extends RuntimeException {
    
    public RoleAlreadyExistsException(String message) {
        super(message);
    }
    
    public RoleAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}