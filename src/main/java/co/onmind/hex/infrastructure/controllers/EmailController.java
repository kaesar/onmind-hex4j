package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.in.SendEmailRequestDto;
import co.onmind.hex.application.ports.in.SendEmailTrait;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@ConditionalOnProperty(name = "app.notification.email.endpoint-enabled", havingValue = "true")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final SendEmailTrait sendEmailTrait;

    public EmailController(SendEmailTrait sendEmailTrait) {
        this.sendEmailTrait = sendEmailTrait;
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody SendEmailRequestDto request) {
        logger.info("Sending email to: {}", request.to());
        sendEmailTrait.sendEmail(request);
        return ResponseEntity.ok(Map.of("message", "Email queued successfully"));
    }
}
