package co.onmind.hex.infrastructure.notification.adapter

import co.onmind.hex.domain.model.Role
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.slf4j.Logger
import java.time.LocalDateTime

class NotificationAdapterTest {
    
    private val adapter = NotificationAdapter()
    
    @Test
    fun `should notify role created`() {
        // Given
        val role = Role(id = 1L, name = "TEST_ROLE", createdAt = LocalDateTime.now())
        
        // When
        adapter.notifyRoleCreated(role)
        
        // Then - No exception should be thrown
    }
    
    @Test
    fun `should notify role updated`() {
        // Given
        val role = Role(id = 1L, name = "UPDATED_ROLE", createdAt = LocalDateTime.now())
        
        // When
        adapter.notifyRoleUpdated(role)
        
        // Then - No exception should be thrown
    }
    
    @Test
    fun `should notify role deleted`() {
        // Given
        val roleId = 1L
        
        // When
        adapter.notifyRoleDeleted(roleId)
        
        // Then - No exception should be thrown
    }
}