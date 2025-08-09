package co.onmind.hex.infrastructure.notification.adapter

import co.onmind.hex.domain.model.Role
import co.onmind.hex.domain.ports.out.NotificationPort
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Adapter that implements the NotificationPort using logging.
 * 
 * This is a simple implementation that logs notifications.
 * In a real application, this could send emails, push notifications,
 * or integrate with external messaging systems.
 */
@Singleton
class NotificationAdapter : NotificationPort {
    
    private val logger = LoggerFactory.getLogger(NotificationAdapter::class.java)
    
    override fun notifyRoleCreated(role: Role) {
        logger.info("Role created notification: Role '${role.name}' with ID ${role.id} was created at ${role.createdAt}")
    }
    
    override fun notifyRoleUpdated(role: Role) {
        logger.info("Role updated notification: Role '${role.name}' with ID ${role.id} was updated")
    }
    
    override fun notifyRoleDeleted(roleId: Long) {
        logger.info("Role deleted notification: Role with ID $roleId was deleted")
    }
}