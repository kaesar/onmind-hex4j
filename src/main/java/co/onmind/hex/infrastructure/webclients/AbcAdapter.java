package co.onmind.hex.infrastructure.webclients;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import co.onmind.hex.transverse.resilience.OutboundGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbcAdapter implements AbcPort {

    private static final Logger logger = LoggerFactory.getLogger(AbcAdapter.class);

    private final AbcWebClient abcWebClient;
    private final OutboundGuard guard;

    public AbcAdapter(AbcWebClient abcWebClient, OutboundGuard abcGuard) {
        this.abcWebClient = abcWebClient;
        this.guard = abcGuard;
    }

    @Override
    public AbcResponse sheet(String show, String from, String some) {
        logger.debug("AbcAdapter.sheet: show={}", show);
        return guard.execute(() -> abcWebClient.sheet(show, from, some));
    }

    @Override
    public AbcResponse exec(AbcRequest request) {
        logger.debug("AbcAdapter.exec: what={}, from={}", request.what(), request.from());
        return guard.execute(() -> abcWebClient.ask(request));
    }
}
