package co.onmind.hex.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

class RoleTest {
    
    @Test
    fun `should create role with valid name`() {
        // Given
        val name = "TEST_ROLE"
        
        // When
        val role = Role.create(name)
        
        // Then
        assertThat(role.name).isEqualTo("TEST_ROLE")
        assertThat(role.id).isNull()
        assertThat(role.createdAt).isNotNull()
    }
    
    @Test
    fun `should normalize role name to uppercase`() {
        // Given
        val name = "test_role"
        
        // When
        val role = Role.create(name)
        
        // Then
        assertThat(role.name).isEqualTo("TEST_ROLE")
    }
    
    @Test
    fun `should throw exception for blank name`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            Role(name = "")
        }
    }
    
    @Test
    fun `should throw exception for name exceeding max length`() {
        // Given
        val longName = "A".repeat(101)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            Role(name = longName)
        }
    }
    
    @Test
    fun `should identify system roles correctly`() {
        // Given
        val systemRole = Role(name = "SYSTEM_ADMIN")
        val adminRole = Role(name = "ADMIN")
        val regularRole = Role(name = "USER")
        
        // When & Then
        assertThat(systemRole.isSystemRole()).isTrue()
        assertThat(adminRole.isSystemRole()).isTrue()
        assertThat(regularRole.isSystemRole()).isFalse()
    }
    
    @Test
    fun `should create role with updated name`() {
        // Given
        val originalRole = Role(name = "OLD_NAME")
        
        // When
        val updatedRole = originalRole.withName("new_name")
        
        // Then
        assertThat(updatedRole.name).isEqualTo("NEW_NAME")
        assertThat(updatedRole.id).isEqualTo(originalRole.id)
        assertThat(updatedRole.createdAt).isEqualTo(originalRole.createdAt)
    }
}