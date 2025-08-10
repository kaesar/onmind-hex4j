package co.onmind.microhex.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateRoleRequest DTO.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@DisplayName("CreateRoleRequest DTO Tests")
class CreateRoleRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid name")
        void shouldPassValidationWithValidName() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("ADMIN");

            // When
            Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation with blank name")
        void shouldFailValidationWithBlankName() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest("");

            // When
            Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(request);

            // Then
            assertFalse(violations.isEmpty());
            assertEquals(1, violations.size());
            assertTrue(violations.iterator().next().getMessage().contains("Role name is required"));
        }

        @Test
        @DisplayName("Should fail validation with null name")
        void shouldFailValidationWithNullName() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest(null);

            // When
            Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(request);

            // Then
            assertFalse(violations.isEmpty());
            assertEquals(1, violations.size());
            assertTrue(violations.iterator().next().getMessage().contains("Role name is required"));
        }

        @Test
        @DisplayName("Should fail validation with name too long")
        void shouldFailValidationWithNameTooLong() {
            // Given
            String longName = "A".repeat(101);
            CreateRoleRequest request = new CreateRoleRequest(longName);

            // When
            Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(request);

            // Then
            assertFalse(violations.isEmpty());
            assertEquals(1, violations.size());
            assertTrue(violations.iterator().next().getMessage().contains("Role name cannot exceed 100 characters"));
        }

        @Test
        @DisplayName("Should pass validation with name at maximum length")
        void shouldPassValidationWithNameAtMaximumLength() {
            // Given
            String maxLengthName = "A".repeat(100);
            CreateRoleRequest request = new CreateRoleRequest(maxLengthName);

            // When
            Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Constructor and Getter Tests")
    class ConstructorAndGetterTests {

        @Test
        @DisplayName("Should create request with name")
        void shouldCreateRequestWithName() {
            // Given
            String expectedName = "ADMIN";

            // When
            CreateRoleRequest request = new CreateRoleRequest(expectedName);

            // Then
            assertEquals(expectedName, request.getName());
        }

        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            CreateRoleRequest request = new CreateRoleRequest();

            // Then
            assertNull(request.getName());
        }

        @Test
        @DisplayName("Should set and get name correctly")
        void shouldSetAndGetNameCorrectly() {
            // Given
            CreateRoleRequest request = new CreateRoleRequest();
            String expectedName = "USER";

            // When
            request.setName(expectedName);

            // Then
            assertEquals(expectedName, request.getName());
        }
    }
}