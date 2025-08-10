package co.onmind.hex.application.handler

import co.onmind.hex.application.dto.CreateRoleRequest
import co.onmind.hex.application.dto.UpdateRoleRequest
import co.onmind.hex.application.mapper.RoleMapper
import co.onmind.hex.domain.model.Role
import co.onmind.hex.domain.ports.`in`.RoleServicePort
import co.onmind.hex.domain.service.RoleAlreadyExistsException
import co.onmind.hex.domain.service.RoleNotFoundException
import co.onmind.hex.domain.service.SystemRoleException
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.assertj.core.api.Assertions.assertThat

class RoleHandlerTest {
    
    private val roleServicePort = mock<RoleServicePort>()
    private val roleMapper = mock<RoleMapper>()
    private val roleHandler = RoleHandler(roleServicePort, roleMapper)
    
    @Test
    fun `should create role successfully`() {
        // Given
        val request = CreateRoleRequest("TEST_ROLE")
        val role = Role(id = 1L, name = "TEST_ROLE")
        val response = co.onmind.hex.application.dto.RoleResponse(1L, "TEST_ROLE", role.createdAt, false)
        
        whenever(roleServicePort.createRole("TEST_ROLE")).thenReturn(role)
        whenever(roleMapper.toResponse(role)).thenReturn(response)
        
        // When
        val result = roleHandler.createRole(request)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body()).isEqualTo(response)
    }
    
    @Test
    fun `should handle role already exists exception`() {
        // Given
        val request = CreateRoleRequest("EXISTING_ROLE")
        whenever(roleServicePort.createRole("EXISTING_ROLE")).thenThrow(RoleAlreadyExistsException("Role exists"))
        
        // When
        val result = roleHandler.createRole(request)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.CONFLICT)
    }
    
    @Test
    fun `should handle illegal argument exception`() {
        // Given
        val request = CreateRoleRequest("")
        whenever(roleServicePort.createRole("")).thenThrow(IllegalArgumentException("Invalid name"))
        
        // When
        val result = roleHandler.createRole(request)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.BAD_REQUEST)
    }
    
    @Test
    fun `should update role successfully`() {
        // Given
        val request = UpdateRoleRequest("UPDATED_ROLE")
        val role = Role(id = 1L, name = "UPDATED_ROLE")
        val response = co.onmind.hex.application.dto.RoleResponse(1L, "UPDATED_ROLE", role.createdAt, false)
        
        whenever(roleServicePort.updateRole(1L, "UPDATED_ROLE")).thenReturn(role)
        whenever(roleMapper.toResponse(role)).thenReturn(response)
        
        // When
        val result = roleHandler.updateRole(1L, request)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.OK)
        assertThat(result.body()).isEqualTo(response)
    }
    
    @Test
    fun `should handle role not found exception on update`() {
        // Given
        val request = UpdateRoleRequest("NEW_NAME")
        whenever(roleServicePort.updateRole(999L, "NEW_NAME")).thenThrow(RoleNotFoundException("Role not found"))
        
        // When
        val result = roleHandler.updateRole(999L, request)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.NOT_FOUND)
    }
    
    @Test
    fun `should handle system role exception on update`() {
        // Given
        val request = UpdateRoleRequest("NEW_NAME")
        whenever(roleServicePort.updateRole(1L, "NEW_NAME")).thenThrow(SystemRoleException("Cannot update system role"))
        
        // When
        val result = roleHandler.updateRole(1L, request)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.FORBIDDEN)
    }
    
    @Test
    fun `should delete role successfully`() {
        // Given
        doNothing().whenever(roleServicePort).deleteRole(1L)
        
        // When
        val result = roleHandler.deleteRole(1L)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.NO_CONTENT)
    }
    
    @Test
    fun `should handle role not found exception on delete`() {
        // Given
        whenever(roleServicePort.deleteRole(999L)).thenThrow(RoleNotFoundException("Role not found"))
        
        // When
        val result = roleHandler.deleteRole(999L)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.NOT_FOUND)
    }
    
    @Test
    fun `should get role by id successfully`() {
        // Given
        val role = Role(id = 1L, name = "TEST_ROLE")
        val response = co.onmind.hex.application.dto.RoleResponse(1L, "TEST_ROLE", role.createdAt, false)
        
        whenever(roleServicePort.getRoleById(1L)).thenReturn(role)
        whenever(roleMapper.toResponse(role)).thenReturn(response)
        
        // When
        val result = roleHandler.getRoleById(1L)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.OK)
        assertThat(result.body()).isEqualTo(response)
    }
    
    @Test
    fun `should return not found when role does not exist`() {
        // Given
        whenever(roleServicePort.getRoleById(999L)).thenReturn(null)
        
        // When
        val result = roleHandler.getRoleById(999L)
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.NOT_FOUND)
    }
    
    @Test
    fun `should get all roles successfully`() {
        // Given
        val roles = listOf(Role(id = 1L, name = "ADMIN"), Role(id = 2L, name = "USER"))
        val responses = listOf(
            co.onmind.hex.application.dto.RoleResponse(1L, "ADMIN", roles[0].createdAt, true),
            co.onmind.hex.application.dto.RoleResponse(2L, "USER", roles[1].createdAt, false)
        )
        
        whenever(roleServicePort.getAllRoles()).thenReturn(roles)
        whenever(roleMapper.toResponseList(roles)).thenReturn(responses)
        
        // When
        val result = roleHandler.getAllRoles()
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.OK)
        assertThat(result.body()).isEqualTo(responses)
    }
    
    @Test
    fun `should search roles successfully`() {
        // Given
        val roles = listOf(Role(id = 1L, name = "ADMIN"))
        val responses = listOf(co.onmind.hex.application.dto.RoleResponse(1L, "ADMIN", roles[0].createdAt, true))
        
        whenever(roleServicePort.searchRolesByName("ADMIN")).thenReturn(roles)
        whenever(roleMapper.toResponseList(roles)).thenReturn(responses)
        
        // When
        val result = roleHandler.searchRoles("ADMIN")
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.OK)
        assertThat(result.body()).isEqualTo(responses)
    }
    
    @Test
    fun `should get role count successfully`() {
        // Given
        whenever(roleServicePort.getRoleCount()).thenReturn(5L)
        
        // When
        val result = roleHandler.getRoleCount()
        
        // Then
        assertThat(result.status).isEqualTo(HttpStatus.OK)
        assertThat(result.body()).isEqualTo(mapOf("count" to 5L))
    }
}