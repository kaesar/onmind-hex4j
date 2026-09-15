package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.ports.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
@Profile("sns")
public class SnsEventSenderAdapter implements EventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(SnsEventSenderAdapter.class);

    private final SnsClient snsClient;
    private final String defaultTopicArn;

    public SnsEventSenderAdapter(SnsClient snsClient,
                                 @Value("${app.sns.topic-arn:}") String defaultTopicArn) {
        this.snsClient = snsClient;
        this.defaultTopicArn = defaultTopicArn;
    }

    @Override
    public void publish(String topic, String key, String payload) {
        String targetArn = topic != null && !topic.isBlank() ? topic : defaultTopicArn;
        logger.debug("Publishing to SNS topic={}, key={}", targetArn, key);

        PublishRequest.Builder request = PublishRequest.builder()
                .topicArn(targetArn)
                .message(payload);

        if (key != null && !key.isBlank()) {
            request.messageStructure("json");
        }

        try {
            PublishResponse result = snsClient.publish(request.build());
            logger.debug("SNS message published to {}, messageId={}", targetArn, result.messageId());
        } catch (Exception e) {
            logger.error("Failed to publish SNS message to {}: {}", targetArn, e.getMessage());
            throw new RuntimeException("SNS publish failed", e);
        }
    }
}
