package co.onmind.microhex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for the Hexagonal Architecture Template.
 * 
 * This application demonstrates a complete implementation of hexagonal architecture
 * (Ports and Adapters pattern) using Spring Boot.
 * 
 * The application includes:
 * - Domain layer with business logic
 * - Application layer with use cases and ports
 * - Infrastructure layer with adapters
 * - Complete CRUD operations for Role entity
 * - H2 in-memory database
 * - Comprehensive testing strategy
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "co.onmind.microhex.infrastructure.persistence.repositories")
public class MicroHexApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroHexApplication.class, args);
    }
}