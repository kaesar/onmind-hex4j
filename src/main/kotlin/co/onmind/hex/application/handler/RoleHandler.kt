package co.onmind.hex.application.handler

import co.onmind.hex.application.dto.CreateRoleRequest
import co.onmind.hex.application.dto.RoleResponse
import co.onmind.hex.application.dto.UpdateRoleRequest
import co.onmind.hex.application.mapper.RoleMapper
import co.onmind.hex.domain.ports.`in`.RoleServicePort
import co.onmind.hex.domain.service.RoleAlreadyExistsException
import co.onmind.hex.domain.service.RoleNotFoundException
import co.onmind.hex.domain.service.SystemRoleException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton

@Singleton
class RoleHandler(
    private val roleServicePort: RoleServicePort,
    private val roleMapper: RoleMapper
) {
    
    fun createRole(request: CreateRoleRequest): HttpResponse<RoleResponse> {
        return try {
            val role = roleServicePort.createRole(request.name)
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
    
    fun updateRole(id: Long, request: UpdateRoleRequest): HttpResponse<RoleResponse> {
        return try {
            val role = roleServicePort.updateRole(id, request.name)
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
    
    fun deleteRole(id: Long): HttpResponse<Void> {
        return try {
            roleServicePort.deleteRole(id)
            HttpResponse.noContent()
        } catch (e: Exception) {
            when (e) {
                is RoleNotFoundException -> HttpResponse.notFound()
                is SystemRoleException -> HttpResponse.status<Void>(HttpStatus.FORBIDDEN)
                else -> HttpResponse.serverError()
            }
        }
    }
    
    fun getRoleById(id: Long): HttpResponse<RoleResponse> {
        val role = roleServicePort.getRoleById(id)
        return if (role != null) {
            val response = roleMapper.toResponse(role)
            HttpResponse.ok(response)
        } else {
            HttpResponse.notFound()
        }
    }
    
    fun getAllRoles(): HttpResponse<List<RoleResponse>> {
        val roles = roleServicePort.getAllRoles()
        val responses = roleMapper.toResponseList(roles)
        return HttpResponse.ok(responses)
    }
    
    fun searchRoles(name: String): HttpResponse<List<RoleResponse>> {
        return try {
            val roles = roleServicePort.searchRolesByName(name)
            val responses = roleMapper.toResponseList(roles)
            HttpResponse.ok(responses)
        } catch (e: IllegalArgumentException) {
            HttpResponse.badRequest()
        }
    }
    
    fun getRoleCount(): HttpResponse<Map<String, Long>> {
        val count = roleServicePort.getRoleCount()
        return HttpResponse.ok(mapOf("count" to count))
    }
}