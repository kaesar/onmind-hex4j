package co.onmind.microhex.domain.exceptions;

/**
 * Exception thrown when a requested role is not found.
 */
public class RoleNotFoundException extends RuntimeException {
    
    public RoleNotFoundException(String message) {
        super(message);
    }
    
    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}