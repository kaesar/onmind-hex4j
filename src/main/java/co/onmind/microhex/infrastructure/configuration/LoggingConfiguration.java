package co.onmind.microhex.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuration class for logging-related components.
 * 
 * This configuration sets up logging infrastructure including:
 * - Request/Response logging filter
 * - Structured logging configuration
 * - Performance monitoring setup
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Configuration
public class LoggingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);

    /**
     * Configures request logging filter to log incoming HTTP requests.
     * This helps with debugging and monitoring API usage.
     * 
     * @return CommonsRequestLoggingFilter configured for the application
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        
        logger.info("Request logging filter configured successfully");
        return filter;
    }
}