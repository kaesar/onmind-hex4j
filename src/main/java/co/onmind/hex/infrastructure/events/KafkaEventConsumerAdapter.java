package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.dto.in.KafkaScriptCommand;
import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaEventConsumerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventConsumerAdapter.class);

    private final ExecuteScriptTrait executeScriptTrait;
    private final EventPublisherPort eventPublisher;
    private final ObjectMapper objectMapper;
    private final String resultsTopic;

    public KafkaEventConsumerAdapter(
            ExecuteScriptTrait executeScriptTrait,
            EventPublisherPort eventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic.script-results:hex4j.script.results}") String resultsTopic) {
        this.executeScriptTrait = executeScriptTrait;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.resultsTopic = resultsTopic;
    }

    @KafkaListener(topics = "${app.kafka.topic.script-commands:hex4j.script.commands}")
    public void onScriptCommand(String message) {
        logger.debug("Received Kafka command: {}", message);

        KafkaScriptCommand command;
        try {
            command = objectMapper.readValue(message, KafkaScriptCommand.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize command: {}", e.getMessage());
            return;
        }

        try {
            ScriptResultResponseDto result = executeScriptTrait.executeScript(command.script());
            publishResult(command.correlationId(), result, null);
        } catch (Exception e) {
            logger.error("Script execution failed: {}", e.getMessage());
            try {
                publishResult(command.correlationId(), null, e.getMessage());
            } catch (Exception publishError) {
                logger.error("Failed to publish result: {}", publishError.getMessage());
            }
        }
    }

    private void publishResult(String correlationId, ScriptResultResponseDto result, String error) {
        try {
            String payload = objectMapper.writeValueAsString(
                new ScriptResultEnvelope(correlationId, result, error)
            );
            eventPublisher.publish(resultsTopic, correlationId, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize/publish result", e);
        }
    }

    private record ScriptResultEnvelope(
            String correlationId,
            ScriptResultResponseDto result,
            String error
    ) {}
}
