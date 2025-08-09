package co.onmind.hex.infrastructure.controllers

import co.onmind.hex.application.dto.CreateRoleRequest
import co.onmind.hex.application.dto.RoleResponse
import co.onmind.hex.application.dto.UpdateRoleRequest
import co.onmind.hex.application.handler.RoleHandler
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Controller("/api/v1/roles")
@Validated
class RoleController(
    private val roleHandler: RoleHandler
) {
    
    @Post
    fun createRole(@Valid @Body request: CreateRoleRequest): HttpResponse<RoleResponse> {
        return roleHandler.createRole(request)
    }
    
    @Put("/{id}")
    fun updateRole(
        @Min(1) id: Long,
        @Valid @Body request: UpdateRoleRequest
    ): HttpResponse<RoleResponse> {
        return roleHandler.updateRole(id, request)
    }
    
    @Delete("/{id}")
    fun deleteRole(@Min(1) id: Long): HttpResponse<Void> {
        return roleHandler.deleteRole(id)
    }
    
    @Get("/{id}")
    fun getRoleById(@Min(1) id: Long): HttpResponse<RoleResponse> {
        return roleHandler.getRoleById(id)
    }
    
    @Get
    fun getAllRoles(): HttpResponse<List<RoleResponse>> {
        return roleHandler.getAllRoles()
    }
    
    @Get("/search")
    fun searchRoles(@QueryValue @NotBlank name: String): HttpResponse<List<RoleResponse>> {
        return roleHandler.searchRoles(name)
    }
    
    @Get("/count")
    fun getRoleCount(): HttpResponse<Map<String, Long>> {
        return roleHandler.getRoleCount()
    }
}