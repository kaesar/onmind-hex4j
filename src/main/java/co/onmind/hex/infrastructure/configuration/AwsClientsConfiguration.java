package co.onmind.hex.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsClientsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AwsClientsConfiguration.class);

    @Bean
    @Profile("sqs")
    public SqsClient sqsClient(
            @Value("${app.aws.region:us-east-1}") String region,
            @Value("${app.aws.endpoint:#{null}}") String endpoint) {
        logger.info("Creating SQS sync client region={} endpoint={}", region, endpoint);
        var builder = SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Bean
    @Profile("sns")
    public SnsClient snsClient(
            @Value("${app.aws.region:us-east-1}") String region,
            @Value("${app.aws.endpoint:#{null}}") String endpoint) {
        logger.info("Creating SNS sync client region={} endpoint={}", region, endpoint);
        var builder = SnsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Bean
    @Profile("eventbridge")
    public EventBridgeClient eventBridgeClient(
            @Value("${app.aws.region:us-east-1}") String region,
            @Value("${app.aws.endpoint:#{null}}") String endpoint) {
        logger.info("Creating EventBridge sync client region={} endpoint={}", region, endpoint);
        var builder = EventBridgeClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
