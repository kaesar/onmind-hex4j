package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.application.ports.out.EmailPort;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import co.onmind.hex.application.ports.out.LambdaPort;
import co.onmind.hex.application.ports.out.ScriptServicesPort;
import co.onmind.hex.application.ports.out.StorePort;
import co.onmind.hex.domain.models.StoreItem;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class ScriptServicesFacade implements ScriptServicesPort {

    private static final Logger logger = LoggerFactory.getLogger(ScriptServicesFacade.class);
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    private final AbcPort abcPort;
    private final ObjectProvider<EventPublisherPort> eventPublisherProvider;
    private final LambdaPort lambdaPort;
    private final StorePort storePort;
    private final EmailPort emailPort;
    private final CachePort cachePort;

    public ScriptServicesFacade(
            AbcPort abcPort,
            ObjectProvider<EventPublisherPort> eventPublisherProvider,
            LambdaPort lambdaPort,
            StorePort storePort,
            EmailPort emailPort,
            CachePort cachePort) {
        this.abcPort = abcPort;
        this.eventPublisherProvider = eventPublisherProvider;
        this.lambdaPort = lambdaPort;
        this.storePort = storePort;
        this.emailPort = emailPort;
        this.cachePort = cachePort;
    }

    @Override
    public AbcResponse abcSheet(String show, String from, String some) {
        logger.debug("Script.services.abcSheet: show={}, from={}, some={}", show, from, some);
        return abcPort.sheet(show, from, some);
    }

    @Override
    public AbcResponse abcExec(String what, String from, String some, String with, String puts) {
        AbcRequest request = AbcRequest.builder()
            .what(what).from(from).some(some).with(with).puts(puts).build();
        logger.debug("Script.services.abcExec: what={}, from={}", what, from);
        return abcPort.exec(request);
    }

    @Override
    public void publish(String topic, String key, String payload) {
        EventPublisherPort publisher = eventPublisherProvider.getIfAvailable();
        if (publisher == null) {
            throw new UnsupportedOperationException(
                "No event publisher available. Activate a profile (kafka, sqs, sns, eventbridge, rabbitmq).");
        }
        publisher.publish(topic, key, payload);
    }

    @Override
    public String invoke(String functionName, String payload) {
        return lambdaPort.invoke(functionName, payload);
    }

    @Override
    public void invokeAsync(String functionName, String payload) {
        lambdaPort.invokeAsync(functionName, payload);
    }

    @Override
    public List<StoreItem> listItems(String bucket) {
        return storePort.listItems(bucket);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        emailPort.send(to, subject, body);
    }

    @Override
    public String cacheGet(String key) {
        return cachePort.get(key);
    }

    @Override
    public void cacheSet(String key, String value) {
        cachePort.set(key, value, DEFAULT_CACHE_TTL);
    }

    @Override
    public void cacheEvict(String key) {
        cachePort.evict(key);
    }
}
