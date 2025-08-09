package co.onmind.hex

import io.micronaut.runtime.Micronaut

/**
 * Main application class for the Hexagonal Architecture with Micronaut.
 * 
 * This application demonstrates a hexagonal architecture implementation
 * using Kotlin, Micronaut, and Gradle KTS with a variation where
 * ports are located in the domain layer.
 */
fun main(args: Array<String>) {
    Micronaut.build()
        .packages("co.onmind.hex")
        .start()
}