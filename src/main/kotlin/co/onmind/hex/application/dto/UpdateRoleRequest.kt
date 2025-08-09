package co.onmind.hex.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateRoleRequest(
    @field:NotBlank(message = "Role name cannot be blank")
    @field:Size(max = 100, message = "Role name cannot exceed 100 characters")
    val name: String
)