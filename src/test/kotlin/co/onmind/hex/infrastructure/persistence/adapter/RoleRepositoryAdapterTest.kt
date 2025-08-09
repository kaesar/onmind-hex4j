package co.onmind.hex.infrastructure.persistence.adapter

import co.onmind.hex.domain.model.Role
import co.onmind.hex.infrastructure.persistence.entity.RoleEntity
import co.onmind.hex.infrastructure.persistence.repository.RoleJdbcRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime
import java.util.*

class RoleRepositoryAdapterTest {
    
    private val jdbcRepository = mock<RoleJdbcRepository>()
    private val adapter = RoleRepositoryAdapter(jdbcRepository)
    
    @Test
    fun `should save role successfully`() {
        // Given
        val role = Role(name = "TEST_ROLE")
        val savedEntity = RoleEntity(id = 1L, name = "TEST_ROLE", createdAt = LocalDateTime.now())
        
        whenever(jdbcRepository.save(any<RoleEntity>())).thenReturn(savedEntity)
        
        // When
        val result = adapter.save(role)
        
        // Then
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo("TEST_ROLE")
        verify(jdbcRepository).save(any<RoleEntity>())
    }
    
    @Test
    fun `should find role by id successfully`() {
        // Given
        val roleId = 1L
        val entity = RoleEntity(id = roleId, name = "TEST_ROLE", createdAt = LocalDateTime.now())
        
        whenever(jdbcRepository.findById(roleId)).thenReturn(Optional.of(entity))
        
        // When
        val result = adapter.findById(roleId)
        
        // Then
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(roleId)
        assertThat(result.name).isEqualTo("TEST_ROLE")
    }
    
    @Test
    fun `should return null when role not found by id`() {
        // Given
        val roleId = 999L
        whenever(jdbcRepository.findById(roleId)).thenReturn(Optional.empty())
        
        // When
        val result = adapter.findById(roleId)
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `should find role by name successfully`() {
        // Given
        val roleName = "TEST_ROLE"
        val entity = RoleEntity(id = 1L, name = roleName, createdAt = LocalDateTime.now())
        
        whenever(jdbcRepository.findByName(roleName)).thenReturn(Optional.of(entity))
        
        // When
        val result = adapter.findByName(roleName)
        
        // Then
        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo(roleName)
    }
    
    @Test
    fun `should find all roles successfully`() {
        // Given
        val entities = listOf(
            RoleEntity(id = 1L, name = "ADMIN", createdAt = LocalDateTime.now()),
            RoleEntity(id = 2L, name = "USER", createdAt = LocalDateTime.now())
        )
        
        whenever(jdbcRepository.findAll()).thenReturn(entities)
        
        // When
        val result = adapter.findAll()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("ADMIN")
        assertThat(result[1].name).isEqualTo("USER")
    }
    
    @Test
    fun `should delete role successfully`() {
        // Given
        val roleId = 1L
        whenever(jdbcRepository.existsById(roleId)).thenReturn(true)
        
        // When
        val result = adapter.deleteById(roleId)
        
        // Then
        assertThat(result).isTrue()
        verify(jdbcRepository).deleteById(roleId)
    }
    
    @Test
    fun `should return false when deleting non-existent role`() {
        // Given
        val roleId = 999L
        whenever(jdbcRepository.existsById(roleId)).thenReturn(false)
        
        // When
        val result = adapter.deleteById(roleId)
        
        // Then
        assertThat(result).isFalse()
        verify(jdbcRepository, never()).deleteById(any())
    }
    
    @Test
    fun `should check if role exists by name`() {
        // Given
        val roleName = "TEST_ROLE"
        whenever(jdbcRepository.existsByName(roleName)).thenReturn(true)
        
        // When
        val result = adapter.existsByName(roleName)
        
        // Then
        assertThat(result).isTrue()
    }
    
    @Test
    fun `should count roles successfully`() {
        // Given
        whenever(jdbcRepository.count()).thenReturn(5L)
        
        // When
        val result = adapter.count()
        
        // Then
        assertThat(result).isEqualTo(5L)
    }
}