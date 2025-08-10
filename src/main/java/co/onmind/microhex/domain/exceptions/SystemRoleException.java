package co.onmind.microhex.domain.exceptions;

/**
 * Exception thrown when attempting to modify or delete a system role.
 */
public class SystemRoleException extends RuntimeException {
    
    public SystemRoleException(String message) {
        super(message);
    }
    
    public SystemRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}