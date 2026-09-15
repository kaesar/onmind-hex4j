package co.onmind.hex.application.ports.out;

import java.util.List;

public interface EmailPort {

    void send(String to, String subject, String body);

    void send(String to, String subject, String body, String from, List<String> cc);
}
