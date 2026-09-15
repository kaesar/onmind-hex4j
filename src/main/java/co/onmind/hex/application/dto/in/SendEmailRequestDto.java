package co.onmind.hex.application.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SendEmailRequestDto(
    @NotBlank(message = "Recipient is required")
    @Email(message = "Recipient must be a valid email")
    String to,
    @NotBlank(message = "Subject is required")
    String subject,
    String from,
    List<@Email(message = "CC must contain valid emails") String> cc,
    @NotBlank(message = "Body is required")
    String body
) {}
