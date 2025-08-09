package co.onmind.hex.domain.service

import co.onmind.hex.domain.model.Role
import co.onmind.hex.domain.ports.`in`.RoleServicePort
import co.onmind.hex.domain.ports.out.NotificationPort
import co.onmind.hex.domain.ports.out.RoleRepositoryPort
import jakarta.inject.Singleton

/**
 * Domain service that orchestrates role operations through ports.
 * 
 * This service contains the business logic for role management,
 * including both commands (write operations) and queries (read operations).
 * It implements the input port and uses output ports to interact with 
 * external systems while maintaining domain independence.
 */
@Singleton
class RoleService(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val notificationPort: NotificationPort
) : RoleServicePort {
    
    // ========== COMMANDS (Write Operations) ==========
    
    /**
     * Creates a new role in the system.
     * Uses modern Java 21 features for better performance and readability.
     */
    override fun createRole(name: String): Role {
        val normalizedName = name.trim().uppercase()
        
        // Business rule: Check if role already exists
        if (roleRepositoryPort.existsByName(normalizedName)) {
            throw RoleAlreadyExistsException("Role with name '$normalizedName' already exists")
        }
        
        val role = Role.create(normalizedName)
        val savedRole = roleRepositoryPort.save(role)
        
        // Notify external systems asynchronously using Virtual Threads (Java 21)
        Thread.startVirtualThread {
            try {
                notificationPort.notifyRoleCreated(savedRole)
            } catch (e: Exception) {
                // Log error but don't fail the main operation
                println("Failed to send notification: ${e.message}")
            }
        }
        
        return savedRole
    }
    
    /**
     * Updates an existing role's name.
     * Uses modern Java 21 features for better performance and readability.
     */
    override fun updateRole(id: Long, newName: String): Role {
        val existingRole = roleRepositoryPort.findById(id)
            ?: throw RoleNotFoundException("Role with ID $id not found")
        
        // Business rule: Cannot update system roles
        if (existingRole.isSystemRole()) {
            throw SystemRoleException("Cannot update system role: ${existingRole.name}")
        }
        
        val normalizedName = newName.trim().uppercase()
        
        // Business rule: Check if new name already exists (excluding current role)
        roleRepositoryPort.findByName(normalizedName)?.let { existing ->
            if (existing.id != id) {
                throw RoleAlreadyExistsException("Role with name '$normalizedName' already exists")
            }
        }
        
        val updatedRole = existingRole.withName(normalizedName)
        val savedRole = roleRepositoryPort.save(updatedRole)
        
        // Notify external systems asynchronously using Virtual Threads (Java 21)
        Thread.startVirtualThread {
            try {
                notificationPort.notifyRoleUpdated(savedRole)
            } catch (e: Exception) {
                println("Failed to send notification: ${e.message}")
            }
        }
        
        return savedRole
    }
    
    /**
     * Deletes a role from the system.
     * Uses modern Java 21 features for better performance and readability.
     */
    override fun deleteRole(id: Long) {
        val role = roleRepositoryPort.findById(id)
            ?: throw RoleNotFoundException("Role with ID $id not found")
        
        // Business rule: Cannot delete system roles
        if (role.isSystemRole()) {
            throw SystemRoleException("Cannot delete system role: ${role.name}")
        }
        
        val deleted = roleRepositoryPort.deleteById(id)
        if (!deleted) {
            throw RoleNotFoundException("Role with ID $id could not be deleted")
        }

        
        // Notify external systems asynchronously using Virtual Threads (Java 21)
        Thread.startVirtualThread {
            try {
                notificationPort.notifyRoleDeleted(id)
            } catch (e: Exception) {
                println("Failed to send notification: ${e.message}")
            }
        }
    }
    
    // ========== QUERIES (Read Operations) ==========
    
    /**
     * Retrieves a role by its ID.
     */
    override fun getRoleById(id: Long): Role? {
        return roleRepositoryPort.findById(id)
    }
    
    /**
     * Retrieves a role by its name.
     */
    override fun getRoleByName(name: String): Role? {
        return roleRepositoryPort.findByName(name.trim().uppercase())
    }
    
    /**
     * Retrieves all roles in the system.
     */
    override fun getAllRoles(): List<Role> {
        return roleRepositoryPort.findAll()
    }
    
    /**
     * Searches roles by name pattern.
     */
    override fun searchRolesByName(pattern: String): List<Role> {
        require(pattern.isNotBlank()) { "Search pattern cannot be blank" }
        return roleRepositoryPort.findByNameContaining(pattern.trim())
    }
    
    /**
     * Gets the total count of roles.
     */
    override fun getRoleCount(): Long {
        return roleRepositoryPort.count()
    }
    
    /**
     * Checks if a role exists by name.
     */
    override fun roleExists(name: String): Boolean {
        return roleRepositoryPort.existsByName(name.trim().uppercase())
    }
}

// ========== Domain Exceptions ==========

class RoleAlreadyExistsException(message: String) : RuntimeException(message)
class RoleNotFoundException(message: String) : RuntimeException(message)
class SystemRoleException(message: String) : RuntimeException(message)