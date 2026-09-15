package co.onmind.hex.domain.exceptions;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String message) {
        super(message);
    }

    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RoleNotFoundException forId(Long roleId) {
        return new RoleNotFoundException("Role with ID " + roleId + " not found");
    }

    public static RoleNotFoundException forName(String roleName) {
        return new RoleNotFoundException("Role with name '" + roleName + "' not found");
    }
}
