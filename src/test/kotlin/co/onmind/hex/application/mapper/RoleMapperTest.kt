package co.onmind.hex.application.mapper

import co.onmind.hex.domain.model.Role
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

class RoleMapperTest {
    
    private val roleMapper = RoleMapper()
    
    @Test
    fun `should map role to response successfully`() {
        // Given
        val role = Role(
            id = 1L,
            name = "TEST_ROLE",
            createdAt = LocalDateTime.now()
        )
        
        // When
        val response = roleMapper.toResponse(role)
        
        // Then
        assertThat(response.id).isEqualTo(1L)
        assertThat(response.name).isEqualTo("TEST_ROLE")
        assertThat(response.createdAt).isEqualTo(role.createdAt)
        assertThat(response.isSystemRole).isFalse()
    }
    
    @Test
    fun `should map system role to response with system flag`() {
        // Given
        val systemRole = Role(
            id = 1L,
            name = "ADMIN",
            createdAt = LocalDateTime.now()
        )
        
        // When
        val response = roleMapper.toResponse(systemRole)
        
        // Then
        assertThat(response.isSystemRole).isTrue()
    }
    
    @Test
    fun `should throw exception when mapping role without id`() {
        // Given
        val roleWithoutId = Role(name = "TEST_ROLE")
        
        // When & Then
        assertThrows<IllegalStateException> {
            roleMapper.toResponse(roleWithoutId)
        }
    }
    
    @Test
    fun `should map list of roles to response list`() {
        // Given
        val roles = listOf(
            Role(id = 1L, name = "ADMIN"),
            Role(id = 2L, name = "USER")
        )
        
        // When
        val responses = roleMapper.toResponseList(roles)
        
        // Then
        assertThat(responses).hasSize(2)
        assertThat(responses[0].name).isEqualTo("ADMIN")
        assertThat(responses[1].name).isEqualTo("USER")
    }
    
    @Test
    fun `should map empty list to empty response list`() {
        // Given
        val emptyList = emptyList<Role>()
        
        // When
        val responses = roleMapper.toResponseList(emptyList)
        
        // Then
        assertThat(responses).isEmpty()
    }
}