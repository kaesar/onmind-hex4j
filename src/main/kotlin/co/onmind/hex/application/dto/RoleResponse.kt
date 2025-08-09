package co.onmind.hex.application.dto

import java.time.LocalDateTime

/**
 * Data Transfer Object for role responses.
 */
data class RoleResponse(
    val id: Long,
    val name: String,
    val createdAt: LocalDateTime,
    val isSystemRole: Boolean
)