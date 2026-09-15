package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.ports.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;

@Component
@Profile("sqs")
public class SqsEventSenderAdapter implements EventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(SqsEventSenderAdapter.class);

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsEventSenderAdapter(SqsClient sqsClient,
                                 @Value("${app.sqs.queue-url:}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void publish(String topic, String key, String payload) {
        String targetQueue = topic != null && !topic.isBlank() ? topic : queueUrl;
        logger.debug("Publishing to SQS queue={}, key={}", targetQueue, key);

        SendMessageRequest.Builder request = SendMessageRequest.builder()
                .queueUrl(targetQueue)
                .messageBody(payload);

        if (key != null && !key.isBlank()) {
            request.messageAttributes(Map.of("key",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(key)
                            .build()));
        }

        try {
            SendMessageResponse result = sqsClient.sendMessage(request.build());
            logger.debug("SQS message sent to {}, messageId={}", targetQueue, result.messageId());
        } catch (Exception e) {
            logger.error("Failed to publish SQS message to {}: {}", targetQueue, e.getMessage());
            throw new RuntimeException("SQS publish failed", e);
        }
    }
}
