package co.onmind.hex.domain.exceptions;

public class DuplicateRoleException extends RuntimeException {

    public DuplicateRoleException(String message) {
        super(message);
    }

    public DuplicateRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateRoleException forName(String roleName) {
        return new DuplicateRoleException("Role with name '" + roleName + "' already exists");
    }

    public static DuplicateRoleException forId(Long roleId) {
        return new DuplicateRoleException("Role with ID " + roleId + " already exists");
    }
}
