package co.onmind.hex.application.handler

import co.onmind.hex.application.dto.CreateRoleRequest
import co.onmind.hex.application.dto.RoleResponse
import co.onmind.hex.application.dto.UpdateRoleRequest
import co.onmind.hex.application.mapper.RoleMapper
import co.onmind.hex.domain.service.RoleAlreadyExistsException
import co.onmind.hex.domain.service.RoleNotFoundException
import co.onmind.hex.domain.service.RoleService
import co.onmind.hex.domain.service.SystemRoleException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Controller("/api/v1/roles")
@Validated
class RoleHandler(
    private val roleService: RoleService,
    private val roleMapper: RoleMapper
) {
    
    @Post
    fun createRole(@Valid @Body request: CreateRoleRequest): HttpResponse<RoleResponse> {
        return try {
            val role = roleService.createRole(request.name)
            val response = roleMapper.toResponse(role)
            HttpResponse.created(response)
        } catch (e: Exception) {
            when (e) {
                is RoleAlreadyExistsException -> HttpResponse.status<RoleResponse>(HttpStatus.CONFLICT)
                is IllegalArgumentException -> HttpResponse.badRequest()
                else -> HttpResponse.serverError()
            }
        }
    }
    
    @Put("/{id}")
    fun updateRole(
        @Min(1) id: Long,
        @Valid @Body request: UpdateRoleRequest
    ): HttpResponse<RoleResponse> {
        return try {
            val role = roleService.updateRole(id, request.name)
            val response = roleMapper.toResponse(role)
            HttpResponse.ok(response)
        } catch (e: Exception) {
            when (e) {
                is RoleNotFoundException -> HttpResponse.notFound()
                is RoleAlreadyExistsException -> HttpResponse.status<RoleResponse>(HttpStatus.CONFLICT)
                is SystemRoleException -> HttpResponse.status<RoleResponse>(HttpStatus.FORBIDDEN)
                is IllegalArgumentException -> HttpResponse.badRequest()
                else -> HttpResponse.serverError()
            }
        }
    }
    
    @Delete("/{id}")
    fun deleteRole(@Min(1) id: Long): HttpResponse<Void> {
        return try {
            roleService.deleteRole(id)
            HttpResponse.noContent()
        } catch (e: Exception) {
            when (e) {
                is RoleNotFoundException -> HttpResponse.notFound()
                is SystemRoleException -> HttpResponse.status<Void>(HttpStatus.FORBIDDEN)
                else -> HttpResponse.serverError()
            }
        }
    }
    
    @Get("/{id}")
    fun getRoleById(@Min(1) id: Long): HttpResponse<RoleResponse> {
        val role = roleService.getRoleById(id)
        return if (role != null) {
            val response = roleMapper.toResponse(role)
            HttpResponse.ok(response)
        } else {
            HttpResponse.notFound()
        }
    }
    
    @Get
    fun getAllRoles(): HttpResponse<List<RoleResponse>> {
        val roles = roleService.getAllRoles()
        val responses = roleMapper.toResponseList(roles)
        return HttpResponse.ok(responses)
    }
    
    @Get("/search")
    fun searchRoles(@QueryValue @NotBlank name: String): HttpResponse<List<RoleResponse>> {
        return try {
            val roles = roleService.searchRolesByName(name)
            val responses = roleMapper.toResponseList(roles)
            HttpResponse.ok(responses)
        } catch (e: IllegalArgumentException) {
            HttpResponse.badRequest()
        }
    }
    
    @Get("/count")
    fun getRoleCount(): HttpResponse<Map<String, Long>> {
        val count = roleService.getRoleCount()
        return HttpResponse.ok(mapOf("count" to count))
    }
}