package co.onmind.hex.infrastructure.web

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

/**
 * Simple role controller to test basic routing.
 */
@Controller("/api/v1/simple-roles")
class SimpleRoleController {
    
    @Get
    fun getAllRoles(): HttpResponse<Map<String, String>> {
        return HttpResponse.ok(mapOf("message" to "Simple roles endpoint working"))
    }
}