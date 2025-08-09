package co.onmind.hex.infrastructure.persistence.entity

import io.micronaut.data.annotation.*
import java.time.LocalDateTime

/**
 * JPA Entity for Role persistence.
 * 
 * This entity represents the database table structure
 * and is used by the infrastructure layer for persistence.
 */
@MappedEntity("roles")
data class RoleEntity(
    @field:Id
    @field:GeneratedValue(GeneratedValue.Type.IDENTITY)
    val id: Long? = null,
    
    val name: String,
    
    @field:DateCreated
    val createdAt: LocalDateTime = LocalDateTime.now()
)