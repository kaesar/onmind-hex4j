package co.onmind.hex.domain.service

import co.onmind.hex.domain.model.Role
import co.onmind.hex.domain.ports.NotificationPort
import co.onmind.hex.domain.ports.RoleRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.assertj.core.api.Assertions.assertThat

class RoleServiceTest {
    
    private val roleRepository = mock<RoleRepository>()
    private val notificationPort = mock<NotificationPort>()
    private val roleService = RoleService(roleRepository, notificationPort)
    
    @Test
    fun `should create role successfully`() {
        // Given
        val roleName = "TEST_ROLE"
        val savedRole = Role(id = 1L, name = "TEST_ROLE")
        
        whenever(roleRepository.existsByName("TEST_ROLE")).thenReturn(false)
        whenever(roleRepository.save(any())).thenReturn(savedRole)
        
        // When
        val result = roleService.createRole(roleName)
        
        // Then
        assertThat(result).isEqualTo(savedRole)
        verify(roleRepository).save(any())
        verify(notificationPort).notifyRoleCreated(savedRole)
    }
    
    @Test
    fun `should throw exception when creating duplicate role`() {
        // Given
        val roleName = "EXISTING_ROLE"
        whenever(roleRepository.existsByName("EXISTING_ROLE")).thenReturn(true)
        
        // When & Then
        assertThrows<RoleAlreadyExistsException> {
            roleService.createRole(roleName)
        }
        
        verify(roleRepository, never()).save(any())
        verify(notificationPort, never()).notifyRoleCreated(any())
    }
    
    @Test
    fun `should get role by id successfully`() {
        // Given
        val roleId = 1L
        val role = Role(id = roleId, name = "TEST_ROLE")
        whenever(roleRepository.findById(roleId)).thenReturn(role)
        
        // When
        val result = roleService.getRoleById(roleId)
        
        // Then
        assertThat(result).isEqualTo(role)
    }
    
    @Test
    fun `should return null when role not found`() {
        // Given
        val roleId = 999L
        whenever(roleRepository.findById(roleId)).thenReturn(null)
        
        // When
        val result = roleService.getRoleById(roleId)
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `should get all roles successfully`() {
        // Given
        val roles = listOf(
            Role(id = 1L, name = "ADMIN"),
            Role(id = 2L, name = "USER")
        )
        whenever(roleRepository.findAll()).thenReturn(roles)
        
        // When
        val result = roleService.getAllRoles()
        
        // Then
        assertThat(result).isEqualTo(roles)
    }
    
    @Test
    fun `should delete role successfully`() {
        // Given
        val roleId = 1L
        val role = Role(id = roleId, name = "TEST_ROLE")
        whenever(roleRepository.findById(roleId)).thenReturn(role)
        whenever(roleRepository.deleteById(roleId)).thenReturn(true)
        
        // When
        roleService.deleteRole(roleId)
        
        // Then
        verify(roleRepository).deleteById(roleId)
        verify(notificationPort).notifyRoleDeleted(roleId, "TEST_ROLE")
    }
    
    @Test
    fun `should throw exception when deleting system role`() {
        // Given
        val roleId = 1L
        val systemRole = Role(id = roleId, name = "ADMIN")
        whenever(roleRepository.findById(roleId)).thenReturn(systemRole)
        
        // When & Then
        assertThrows<SystemRoleException> {
            roleService.deleteRole(roleId)
        }
        
        verify(roleRepository, never()).deleteById(any())
        verify(notificationPort, never()).notifyRoleDeleted(any(), any())
    }
    
    @Test
    fun `should search roles by name pattern`() {
        // Given
        val pattern = "ADMIN"
        val roles = listOf(Role(id = 1L, name = "ADMIN"))
        whenever(roleRepository.findByNameContaining(pattern)).thenReturn(roles)
        
        // When
        val result = roleService.searchRolesByName(pattern)
        
        // Then
        assertThat(result).isEqualTo(roles)
    }
    
    @Test
    fun `should throw exception for blank search pattern`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            roleService.searchRolesByName("")
        }
    }
}