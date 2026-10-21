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
import org.springframework.scheduling.annotation.Scheduled;
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
    private final int visibilityTimeout;

    public SqsEventConsumerAdapter(
            SqsClient sqsClient,
            ExecuteScriptTrait executeScriptTrait,
            EventPublisherPort eventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.sqs.queue-url:}") String queueUrl,
            @Value("${app.sqs.topic.script-results:hex4j.script.results}") String resultsQueueUrl,
            @Value("${app.sqs.visibility-timeout-seconds:120}") int visibilityTimeout) {
        this.sqsClient = sqsClient;
        this.executeScriptTrait = executeScriptTrait;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
        this.resultsQueueUrl = resultsQueueUrl;
        this.visibilityTimeout = visibilityTimeout;
        logger.info("SQS command consumer ready: visibility timeout {}s", visibilityTimeout);
    }

    public List<Message> pollMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .visibilityTimeout(visibilityTimeout)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        messages.forEach(msg -> logger.debug("Received SQS message: id={}", msg.messageId()));
        return messages;
    }

    /**
     * Polls the command queue and processes every message received.
     * <p>
     * Scheduled with a <em>fixed delay</em>, not a fixed rate: the SQS call uses long
     * polling ({@code waitTimeSeconds = 20}), so the task blocks until a message
     * arrives or the wait elapses. A fixed delay guarantees a single poll in flight
     * and never overlaps two of them on the same queue.
     * <p>
     * With virtual threads enabled this long wait costs nothing: the virtual thread
     * unmounts while it waits on the socket, so the scheduler is not blocked.
     * <p>
     * A failure to poll or to process a message is logged and the loop continues:
     * one bad message must not stop the consumer.
     */
    @Scheduled(fixedDelayString = "${app.sqs.poll-interval-ms:1000}")
    public void pollAndProcess() {
        List<Message> messages;
        try {
            messages = pollMessages();
        } catch (Exception e) {
            logger.error("Failed to poll SQS queue: {}", e.getMessage());
            return;
        }

        for (Message message : messages) {
            try {
                processMessage(message);
            } catch (Exception e) {
                logger.error("Failed to process SQS message {}: {}", message.messageId(), e.getMessage());
            }
        }
    }

    public void processMessage(Message message) {
        KafkaScriptCommand command;
        try {
            command = objectMapper.readValue(message.body(), KafkaScriptCommand.class);
        } catch (Exception e) {
            // A malformed payload is a poison message: consume it, otherwise it comes
            // back on every poll.
            logger.error("Failed to deserialize SQS message: {}", e.getMessage());
            deleteMessage(message);
            return;
        }

        ScriptResultResponseDto result;
        try {
            result = executeScriptTrait.executeScript(command.script());
        } catch (Exception e) {
            // A failing script is a business outcome, not a transport failure: publish
            // the error and consume the message. Leaving it in the queue would make it
            // reappear after the visibility timeout and run the script again, forever.
            // If automatic retries are wanted, configure an SQS redrive policy (DLQ).
            logger.error("Script execution failed: {}", e.getMessage());
            publishError(command.correlationId(), e.getMessage());
            deleteMessage(message);
            return;
        }

        // A failure to publish the result must NOT consume the message: the command
        // has to be retried, otherwise the result is lost for good.
        publishResult(command.correlationId(), result, null);
        deleteMessage(message);
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
