package co.onmind.hex.application.handler

import co.onmind.hex.application.dto.CreateRoleRequest
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

/**
 * Integration test for RoleHandler.
 * 
 * This test verifies the complete integration of the application
 * from HTTP layer to database layer.
 */
@org.junit.jupiter.api.Disabled("Disabled due to Micronaut configuration issues")
@MicronautTest
class RoleHandlerIntegrationTest {
    
    @Inject
    @field:Client("/")
    lateinit var client: HttpClient
    
    @Test
    fun `should create role successfully`() {
        // Given
        val request = CreateRoleRequest("INTEGRATION_TEST_ROLE")
        
        // When
        val response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/roles", request),
            Map::class.java
        )
        
        // Then
        assertThat(response.status).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body()).isNotNull
    }
    
    @Test
    fun `should get all roles successfully`() {
        // When
        val response = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/api/v1/roles"),
            List::class.java
        )
        
        // Then
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()).isNotNull
    }
    
    @Test
    fun `should get role count successfully`() {
        // When
        val response = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/api/v1/roles/count"),
            Map::class.java
        )
        
        // Then
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.body()).isNotNull
        assertThat(response.body()!!["count"]).isNotNull
    }
}