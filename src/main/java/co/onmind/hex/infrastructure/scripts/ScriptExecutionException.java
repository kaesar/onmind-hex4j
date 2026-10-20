package co.onmind.hex.infrastructure.scripts;

/**
 * Raised when a script cannot be executed: the executor is saturated, the watchdog
 * timeout fired, the caller was interrupted, or the script itself failed.
 */
public class ScriptExecutionException extends RuntimeException {

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
