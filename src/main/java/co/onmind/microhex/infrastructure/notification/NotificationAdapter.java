package co.onmind.microhex.infrastructure.notification;

import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.domain.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of the NotificationPort for sending notifications
 * about role operations to external systems.
 * 
 * This is a simple implementation that logs notifications.
 * In a real-world scenario, this could send messages to message queues,
 * webhooks, email services, etc.
 */
@Component
public class NotificationAdapter implements NotificationPort {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationAdapter.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRoleCreated(Role role) {
        logger.info("NOTIFICATION: Role created - ID: {}, Name: {}, CreatedAt: {}", 
                   role.getId(), role.getName(), role.getCreatedAt());
        
        // Here you could implement actual notification logic:
        // - Send message to message queue (RabbitMQ, Apache Kafka, etc.)
        // - Send webhook notification
        // - Send email notification
        // - Update external systems
        // - Trigger other business processes
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRoleUpdated(Role role) {
        logger.info("NOTIFICATION: Role updated - ID: {}, Name: {}, CreatedAt: {}", 
                   role.getId(), role.getName(), role.getCreatedAt());
        
        // Here you could implement actual notification logic for updates
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRoleDeleted(Long roleId) {
        logger.info("NOTIFICATION: Role deleted - ID: {}", roleId);
        
        // Here you could implement actual notification logic for deletions
    }
}