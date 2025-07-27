package co.onmind.microhex.infrastructure.configuration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

/**
 * Main application configuration class for the Hexagonal Architecture Template.
 * 
 * This configuration class sets up the core infrastructure components and
 * cross-cutting concerns for the application including:
 * - Transaction management
 * - AOP support for cross-cutting concerns
 * - CORS configuration for web endpoints
 * - Clock bean for time-related operations
 * - Application layer beans configuration
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class ApplicationConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    /**
     * Provides a Clock bean for time-related operations.
     * Using Clock.systemUTC() ensures consistent timezone handling across the application.
     * 
     * @return Clock instance configured for UTC timezone
     */
    @Bean
    public Clock clock() {
        logger.info("Configuring Clock bean with UTC timezone");
        return Clock.systemUTC();
    }



    /**
     * Configures CORS settings for the application.
     * This configuration allows cross-origin requests for development purposes.
     * In production, this should be configured more restrictively.
     * 
     * @param registry CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring CORS mappings for API endpoints");
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}