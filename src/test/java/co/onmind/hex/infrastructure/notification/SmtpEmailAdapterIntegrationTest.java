package co.onmind.hex.infrastructure.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.net.Socket;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class SmtpEmailAdapterIntegrationTest {

    @Autowired
    private JavaMailSender mailSender;

    private static boolean mailpitAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", 1025), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("sends email through Mailpit when available")
    void sendsViaMailpit() {
        assumeTrue(mailpitAvailable(), "Mailpit not available at localhost:1025");
        SmtpEmailAdapter adapter = new SmtpEmailAdapter(mailSender, "hex4j@localhost");
        adapter.send("integration@test.com", "Integration", "hello from hex4j test");
    }
}
