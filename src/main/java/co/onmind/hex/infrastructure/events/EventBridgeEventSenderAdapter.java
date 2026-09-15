package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.ports.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

@Component
@Profile("eventbridge")
public class EventBridgeEventSenderAdapter implements EventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(EventBridgeEventSenderAdapter.class);

    private final EventBridgeClient eventBridgeClient;
    private final String defaultEventBus;

    public EventBridgeEventSenderAdapter(EventBridgeClient eventBridgeClient,
                                         @Value("${app.eventbridge.bus:default}") String defaultEventBus) {
        this.eventBridgeClient = eventBridgeClient;
        this.defaultEventBus = defaultEventBus;
    }

    @Override
    public void publish(String topic, String key, String payload) {
        String eventBus = topic != null && !topic.isBlank() ? topic : defaultEventBus;
        logger.debug("Publishing to EventBridge bus={}, key={}", eventBus, key);

        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName(eventBus)
                .detailType("ScriptExecution")
                .source("hex4j.application")
                .detail(payload)
                .build();

        PutEventsRequest request = PutEventsRequest.builder()
                .entries(entry)
                .build();

        try {
            PutEventsResponse result = eventBridgeClient.putEvents(request);
            logger.debug("EventBridge event published to {}, eventId={}",
                eventBus, result.entries().get(0).eventId());
        } catch (Exception e) {
            logger.error("Failed to publish EventBridge event to {}: {}", eventBus, e.getMessage());
            throw new RuntimeException("EventBridge publish failed", e);
        }
    }
}
