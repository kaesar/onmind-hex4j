package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.in.SendEmailRequestDto;
import co.onmind.hex.application.ports.in.SendEmailTrait;
import co.onmind.hex.application.ports.out.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SendEmailUseCase implements SendEmailTrait {

    private static final Logger logger = LoggerFactory.getLogger(SendEmailUseCase.class);

    private final EmailPort emailPort;

    public SendEmailUseCase(EmailPort emailPort) {
        this.emailPort = emailPort;
    }

    @Override
    public void sendEmail(SendEmailRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("SendEmailRequestDto cannot be null");
        }
        logger.debug("Dispatching email to={}", request.to());
        emailPort.send(
            request.to(),
            request.subject(),
            request.body(),
            request.from(),
            request.cc() != null ? request.cc() : List.of());
    }
}
