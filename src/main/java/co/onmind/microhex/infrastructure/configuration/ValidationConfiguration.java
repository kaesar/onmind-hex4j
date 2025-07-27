package co.onmind.microhex.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

/**
 * Configuration class for validation-related components.
 * 
 * This configuration sets up validation infrastructure including:
 * - Bean validation factory
 * - Method-level validation
 * - Custom validation constraints
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Configuration
public class ValidationConfiguration {

    /**
     * Configures the validator factory bean for Bean Validation.
     * 
     * @return LocalValidatorFactoryBean configured for the application
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Enables method-level validation using @Validated annotation.
     * 
     * @return MethodValidationPostProcessor configured with default validator
     */
    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}