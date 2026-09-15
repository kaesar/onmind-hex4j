package co.onmind.hex.infrastructure.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpEmailAdapterTest {

    @Mock private JavaMailSender mailSender;

    @Test
    @DisplayName("send builds message with default from")
    void sendDefault() {
        SmtpEmailAdapter adapter = new SmtpEmailAdapter(mailSender, "noreply@test.com");
        adapter.send("to@test.com", "subject", "body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("noreply@test.com", message.getFrom());
        assertArrayEquals(new String[]{"to@test.com"}, message.getTo());
        assertEquals("subject", message.getSubject());
        assertEquals("body", message.getText());
    }

    @Test
    @DisplayName("send honors custom from and cc")
    void sendCustom() {
        SmtpEmailAdapter adapter = new SmtpEmailAdapter(mailSender, "noreply@test.com");
        adapter.send("to@test.com", "subject", "body", "custom@test.com", List.of("cc@test.com"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("custom@test.com", message.getFrom());
        assertArrayEquals(new String[]{"cc@test.com"}, message.getCc());
    }
}
