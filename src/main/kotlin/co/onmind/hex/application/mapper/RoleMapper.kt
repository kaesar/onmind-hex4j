package co.onmind.hex.application.mapper

import co.onmind.hex.application.dto.RoleResponse
import co.onmind.hex.domain.model.Role
import jakarta.inject.Singleton

/**
 * Mapper for converting between domain models and DTOs.
 * 
 * This mapper handles the transformation between the domain layer
 * and the application layer, ensuring proper data conversion
 * while maintaining separation of concerns.
 */
@Singleton
class RoleMapper {
    
    /**
     * Converts a Role domain model to RoleResponse DTO.
     */
    fun toResponse(role: Role): RoleResponse {
        return RoleResponse(
            id = role.id ?: throw IllegalStateException("Role ID cannot be null for response"),
            name = role.name,
            createdAt = role.createdAt,
            isSystemRole = role.isSystemRole()
        )
    }
    
    /**
     * Converts a list of Role domain models to RoleResponse DTOs.
     */
    fun toResponseList(roles: List<Role>): List<RoleResponse> {
        return roles.map { toResponse(it) }
    }
}