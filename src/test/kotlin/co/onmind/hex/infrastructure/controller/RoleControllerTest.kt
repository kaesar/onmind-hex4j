package co.onmind.hex.infrastructure.controller

import co.onmind.hex.application.dto.CreateRoleRequest
import co.onmind.hex.application.dto.RoleResponse
import co.onmind.hex.application.dto.UpdateRoleRequest
import co.onmind.hex.application.handler.RoleHandler
import io.micronaut.http.HttpResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

class RoleControllerTest {
    
    private val roleHandler = mock<RoleHandler>()
    private val roleController = RoleController(roleHandler)
    
    @Test
    fun `should delegate create role to handler`() {
        // Given
        val request = CreateRoleRequest("TEST_ROLE")
        val response = RoleResponse(1L, "TEST_ROLE", LocalDateTime.now(), false)
        val httpResponse = HttpResponse.created(response)
        
        whenever(roleHandler.createRole(request)).thenReturn(httpResponse)
        
        // When
        val result = roleController.createRole(request)
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).createRole(request)
    }
    
    @Test
    fun `should delegate update role to handler`() {
        // Given
        val request = UpdateRoleRequest("UPDATED_ROLE")
        val response = RoleResponse(1L, "UPDATED_ROLE", LocalDateTime.now(), false)
        val httpResponse = HttpResponse.ok(response)
        
        whenever(roleHandler.updateRole(1L, request)).thenReturn(httpResponse)
        
        // When
        val result = roleController.updateRole(1L, request)
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).updateRole(1L, request)
    }
    
    @Test
    fun `should delegate delete role to handler`() {
        // Given
        val httpResponse = HttpResponse.noContent<Void>()
        whenever(roleHandler.deleteRole(1L)).thenReturn(httpResponse)
        
        // When
        val result = roleController.deleteRole(1L)
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).deleteRole(1L)
    }
    
    @Test
    fun `should delegate get role by id to handler`() {
        // Given
        val response = RoleResponse(1L, "TEST_ROLE", LocalDateTime.now(), false)
        val httpResponse = HttpResponse.ok(response)
        
        whenever(roleHandler.getRoleById(1L)).thenReturn(httpResponse)
        
        // When
        val result = roleController.getRoleById(1L)
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).getRoleById(1L)
    }
    
    @Test
    fun `should delegate get all roles to handler`() {
        // Given
        val responses = listOf(RoleResponse(1L, "ADMIN", LocalDateTime.now(), true))
        val httpResponse = HttpResponse.ok(responses)
        
        whenever(roleHandler.getAllRoles()).thenReturn(httpResponse)
        
        // When
        val result = roleController.getAllRoles()
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).getAllRoles()
    }
    
    @Test
    fun `should delegate search roles to handler`() {
        // Given
        val responses = listOf(RoleResponse(1L, "ADMIN", LocalDateTime.now(), true))
        val httpResponse = HttpResponse.ok(responses)
        
        whenever(roleHandler.searchRoles("ADMIN")).thenReturn(httpResponse)
        
        // When
        val result = roleController.searchRoles("ADMIN")
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).searchRoles("ADMIN")
    }
    
    @Test
    fun `should delegate get role count to handler`() {
        // Given
        val countMap = mapOf("count" to 5L)
        val httpResponse = HttpResponse.ok(countMap)
        
        whenever(roleHandler.getRoleCount()).thenReturn(httpResponse)
        
        // When
        val result = roleController.getRoleCount()
        
        // Then
        assertThat(result).isEqualTo(httpResponse)
        verify(roleHandler).getRoleCount()
    }
}