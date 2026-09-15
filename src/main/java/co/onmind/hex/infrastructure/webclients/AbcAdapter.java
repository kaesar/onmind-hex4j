package co.onmind.hex.infrastructure.webclients;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import co.onmind.hex.transverse.resilience.CircuitBreakerGeneric;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbcAdapter implements AbcPort {

    private static final Logger logger = LoggerFactory.getLogger(AbcAdapter.class);

    private final AbcWebClient abcWebClient;
    private final CircuitBreaker circuitBreaker;

    public AbcAdapter(AbcWebClient abcWebClient, CircuitBreaker abcCircuitBreaker) {
        this.abcWebClient = abcWebClient;
        this.circuitBreaker = abcCircuitBreaker;
    }

    @Override
    public AbcResponse sheet(String show, String from, String some) {
        logger.debug("AbcAdapter.sheet: show={}", show);
        return CircuitBreakerGeneric.withCircuitBreaker(
            () -> abcWebClient.sheet(show, from, some), circuitBreaker);
    }

    @Override
    public AbcResponse exec(AbcRequest request) {
        logger.debug("AbcAdapter.exec: what={}, from={}", request.what(), request.from());
        return CircuitBreakerGeneric.withCircuitBreaker(
            () -> abcWebClient.ask(request), circuitBreaker);
    }
}
