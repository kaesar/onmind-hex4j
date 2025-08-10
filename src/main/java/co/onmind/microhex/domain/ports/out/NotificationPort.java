package co.onmind.microhex.domain.ports.out;

import co.onmind.microhex.domain.models.Role;

/**
 * Output port for notification operations.
 * This interface defines the contract for sending notifications
 * about role operations to external systems.
 */
public interface NotificationPort {
    
    /**
     * Notifies external systems about role creation.
     * @param role the created role
     */
    void notifyRoleCreated(Role role);
    
    /**
     * Notifies external systems about role update.
     * @param role the updated role
     */
    void notifyRoleUpdated(Role role);
    
    /**
     * Notifies external systems about role deletion.
     * @param roleId the deleted role ID
     */
    void notifyRoleDeleted(Long roleId);
}