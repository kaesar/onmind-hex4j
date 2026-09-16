package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.dto.in.KafkaScriptCommand;
import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbitmq")
public class RabbitMQEventConsumerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQEventConsumerAdapter.class);

    private final ExecuteScriptTrait executeScriptTrait;
    private final EventPublisherPort eventPublisher;
    private final ObjectMapper objectMapper;
    private final String resultsExchange;

    public RabbitMQEventConsumerAdapter(
            ExecuteScriptTrait executeScriptTrait,
            EventPublisherPort eventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.rabbitmq.exchange.results:hex4j.script.results}") String resultsExchange) {
        this.executeScriptTrait = executeScriptTrait;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.resultsExchange = resultsExchange;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.script-commands:hex4j.script.commands}")
    public void onScriptCommand(String message) {
        logger.debug("Received RabbitMQ command: {}", message);

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
            eventPublisher.publish(resultsExchange, correlationId, payload);
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
