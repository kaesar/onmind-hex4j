package co.onmind.hex.infrastructure.webclients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NotificationRequest(

    @JsonProperty("eventType")
    @NotBlank(message = "Event type cannot be blank")
    String eventType,

    @JsonProperty("message")
    @NotBlank(message = "Message cannot be blank")
    String message,

    @JsonProperty("source")
    @NotBlank(message = "Source cannot be blank")
    String source,

    @JsonProperty("referenceId")
    String referenceId,

    @JsonProperty("timestamp")
    @NotNull(message = "Timestamp cannot be null")
    LocalDateTime timestamp
) {

    public NotificationRequest(String eventType, String message, String source, String referenceId) {
        this(eventType, message, source, referenceId, LocalDateTime.now());
    }

    public static NotificationRequest forRoleCreated(String roleName, Long roleId) {
        return new NotificationRequest(
            "ROLE_CREATED",
            String.format("Role '%s' has been created successfully", roleName),
            "hex4j",
            roleId.toString()
        );
    }

    public static NotificationRequest forRoleUpdated(String roleName, Long roleId) {
        return new NotificationRequest(
            "ROLE_UPDATED",
            String.format("Role '%s' has been updated successfully", roleName),
            "hex4j",
            roleId.toString()
        );
    }

    public static NotificationRequest forRoleDeleted(String roleName, Long roleId) {
        return new NotificationRequest(
            "ROLE_DELETED",
            String.format("Role '%s' has been deleted successfully", roleName),
            "hex4j",
            roleId.toString()
        );
    }
}
