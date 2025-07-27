package co.onmind.microhex.application.dto.in;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CreateRoleRequestDto validation.
 * 
 * This test class verifies that the validation annotations on the
 * CreateRoleRequestDto record work correctly.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
class CreateRoleRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidName() {
        // Given
        CreateRoleRequestDto dto = new CreateRoleRequestDto("ADMIN");

        // When
        Set<ConstraintViolation<CreateRoleRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWithBlankName() {
        // Given
        CreateRoleRequestDto dto = new CreateRoleRequestDto("");

        // When
        Set<ConstraintViolation<CreateRoleRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        // Check that at least one of the expected validation messages is present
        boolean hasExpectedMessage = violations.stream()
            .anyMatch(v -> v.getMessage().equals("Role name cannot be blank") || 
                          v.getMessage().equals("Role name must be between 1 and 100 characters"));
        assertThat(hasExpectedMessage).isTrue();
    }

    @Test
    void shouldFailValidationWithNullName() {
        // Given
        CreateRoleRequestDto dto = new CreateRoleRequestDto(null);

        // When
        Set<ConstraintViolation<CreateRoleRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Role name cannot be blank");
    }

    @Test
    void shouldFailValidationWithNameTooLong() {
        // Given
        String longName = "A".repeat(101); // 101 characters
        CreateRoleRequestDto dto = new CreateRoleRequestDto(longName);

        // When
        Set<ConstraintViolation<CreateRoleRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Role name must be between 1 and 100 characters");
    }

    @Test
    void shouldPassValidationWithNameAtMaxLength() {
        // Given
        String maxLengthName = "A".repeat(100); // 100 characters
        CreateRoleRequestDto dto = new CreateRoleRequestDto(maxLengthName);

        // When
        Set<ConstraintViolation<CreateRoleRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWithSingleCharacterName() {
        // Given
        CreateRoleRequestDto dto = new CreateRoleRequestDto("A");

        // When
        Set<ConstraintViolation<CreateRoleRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }
}