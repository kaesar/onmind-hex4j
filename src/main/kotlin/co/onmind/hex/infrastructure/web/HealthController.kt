package co.onmind.hex.infrastructure.web

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

/**
 * Simple health check controller to test if routing is working.
 */
@Controller("/health")
class HealthController {
    
    @Get
    fun health(): HttpResponse<Map<String, String>> {
        return HttpResponse.ok(mapOf("status" to "UP"))
    }
}