package co.onmind.hex.application.dto

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

class DTOTest {
    
    @Test
    fun `should create CreateRoleRequest with valid name`() {
        // Given
        val name = "TEST_ROLE"
        
        // When
        val request = CreateRoleRequest(name)
        
        // Then
        assertThat(request.name).isEqualTo(name)
    }
    
    @Test
    fun `should create UpdateRoleRequest with valid name`() {
        // Given
        val name = "UPDATED_ROLE"
        
        // When
        val request = UpdateRoleRequest(name)
        
        // Then
        assertThat(request.name).isEqualTo(name)
    }
    
    @Test
    fun `should create RoleResponse with all properties`() {
        // Given
        val id = 1L
        val name = "TEST_ROLE"
        val createdAt = LocalDateTime.now()
        val isSystemRole = false
        
        // When
        val response = RoleResponse(id, name, createdAt, isSystemRole)
        
        // Then
        assertThat(response.id).isEqualTo(id)
        assertThat(response.name).isEqualTo(name)
        assertThat(response.createdAt).isEqualTo(createdAt)
        assertThat(response.isSystemRole).isEqualTo(isSystemRole)
    }
    
    @Test
    fun `should create system role response`() {
        // Given
        val id = 1L
        val name = "ADMIN"
        val createdAt = LocalDateTime.now()
        val isSystemRole = true
        
        // When
        val response = RoleResponse(id, name, createdAt, isSystemRole)
        
        // Then
        assertThat(response.isSystemRole).isTrue()
    }
}