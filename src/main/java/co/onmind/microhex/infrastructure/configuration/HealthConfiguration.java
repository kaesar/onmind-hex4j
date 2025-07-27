package co.onmind.microhex.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for health check components.
 * 
 * This configuration sets up health monitoring infrastructure for the application.
 * The Spring Boot Actuator provides default health endpoints, and this configuration
 * can be extended to add custom health indicators as needed.
 * 
 * Available health endpoints:
 * - /actuator/health - Overall application health
 * - /actuator/health/db - Database health (provided by Spring Boot)
 * - /actuator/info - Application information
 * - /actuator/metrics - Application metrics
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Configuration
public class HealthConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HealthConfiguration.class);

    /**
     * Constructor that logs the health configuration initialization.
     */
    public HealthConfiguration() {
        logger.info("Health monitoring configuration initialized");
        logger.info("Health endpoints available at /actuator/health");
        logger.info("Application info available at /actuator/info");
        logger.info("Metrics available at /actuator/metrics");
    }
}