package co.onmind.hex.domain.model

import java.time.LocalDateTime

/**
 * Role domain model representing a user role in the system.
 * 
 * This is the core domain entity that encapsulates the business logic
 * and rules for roles. It follows the hexagonal architecture principles
 * by being independent of external frameworks and infrastructure.
 */
data class Role(
    val id: Long? = null,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    init {
        require(name.isNotBlank()) { "Role name cannot be blank" }
        require(name.length <= 100) { "Role name cannot exceed 100 characters" }
    }
    
    /**
     * Validates if this role is a system role that cannot be deleted.
     */
    fun isSystemRole(): Boolean {
        return name.startsWith("SYSTEM_") || name in RESERVED_NAMES
    }
    
    /**
     * Creates a copy of this role with updated name.
     */
    fun withName(newName: String): Role {
        return copy(name = newName.trim().uppercase())
    }
    
    companion object {
        private val RESERVED_NAMES = setOf("ADMIN", "ROOT", "SYSTEM")
        
        /**
         * Factory method to create a new role with normalized name.
         */
        fun create(name: String): Role {
            val normalizedName = name.trim().uppercase()
            return Role(name = normalizedName)
        }
    }
}