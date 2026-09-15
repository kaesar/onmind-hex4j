package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.in.SendEmailRequestDto;

public interface SendEmailTrait {

    void sendEmail(SendEmailRequestDto request);
}
