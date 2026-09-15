package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.dto.in.KafkaScriptCommand;
import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
@Profile("sqs")
public class SqsEventConsumerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SqsEventConsumerAdapter.class);

    private final SqsClient sqsClient;
    private final ExecuteScriptTrait executeScriptTrait;
    private final EventPublisherPort eventPublisher;
    private final ObjectMapper objectMapper;
    private final String queueUrl;
    private final String resultsQueueUrl;

    public SqsEventConsumerAdapter(
            SqsClient sqsClient,
            ExecuteScriptTrait executeScriptTrait,
            EventPublisherPort eventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.sqs.queue-url:}") String queueUrl,
            @Value("${app.sqs.topic.script-results:hex4j.script.results}") String resultsQueueUrl) {
        this.sqsClient = sqsClient;
        this.executeScriptTrait = executeScriptTrait;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
        this.resultsQueueUrl = resultsQueueUrl;
    }

    public List<Message> pollMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .visibilityTimeout(30)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        messages.forEach(msg -> logger.debug("Received SQS message: id={}", msg.messageId()));
        return messages;
    }

    public void processMessage(Message message) {
        KafkaScriptCommand command;
        try {
            command = objectMapper.readValue(message.body(), KafkaScriptCommand.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize SQS message: {}", e.getMessage());
            deleteMessage(message);
            return;
        }

        try {
            ScriptResultResponseDto result = executeScriptTrait.executeScript(command.script());
            publishResult(command.correlationId(), result, null);
            deleteMessage(message);
        } catch (Exception e) {
            logger.error("Script execution failed: {}", e.getMessage());
            publishError(command.correlationId(), e.getMessage());
        }
    }

    public void deleteMessage(Message message) {
        DeleteMessageRequest request = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsClient.deleteMessage(request);
    }

    private void publishResult(String correlationId, ScriptResultResponseDto result, String error) {
        try {
            String payload = objectMapper.writeValueAsString(
                    new ScriptResultEnvelope(correlationId, result, error)
            );
            eventPublisher.publish(resultsQueueUrl, correlationId, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize/publish SQS result", e);
        }
    }

    private void publishError(String correlationId, String errorMessage) {
        publishResult(correlationId, null, errorMessage);
    }

    private record ScriptResultEnvelope(
            String correlationId,
            ScriptResultResponseDto result,
            String error
    ) {}
}
