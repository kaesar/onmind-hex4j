package co.onmind.hex.infrastructure.web

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/test")
class TestController {
    
    @Get
    fun test(): String = "Working!"
}