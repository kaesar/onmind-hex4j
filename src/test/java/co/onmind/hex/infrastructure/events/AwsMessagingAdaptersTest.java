package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsMessagingAdaptersTest {

    @Mock private SqsClient sqsClient;
    @Mock private SnsClient snsClient;
    @Mock private EventBridgeClient eventBridgeClient;
    @Mock private ExecuteScriptTrait executeScriptTrait;
    @Mock private EventPublisherPort eventPublisher;

    private final ObjectMapper objectMapper = new JsonMapper();

    @Test
    @DisplayName("SqsEventSenderAdapter sends message with key attribute")
    void sqsSender() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().messageId("mid-1").build());

        SqsEventSenderAdapter adapter = new SqsEventSenderAdapter(sqsClient, "default-queue");
        adapter.publish("my-queue", "my-key", "payload");

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertEquals("my-queue", captor.getValue().queueUrl());
        assertEquals("payload", captor.getValue().messageBody());
        assertTrue(captor.getValue().messageAttributes().containsKey("key"));
    }

    @Test
    @DisplayName("SqsEventConsumerAdapter polls, processes and deletes")
    void sqsConsumer() {
        Message message = Message.builder()
            .messageId("m-1")
            .receiptHandle("rh-1")
            .body("{\"script\":\"hello.js\",\"correlationId\":\"req-3\"}")
            .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(executeScriptTrait.executeScript("hello.js"))
            .thenReturn(new ScriptResultResponseDto("hi", "", null));
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
            .thenReturn(DeleteMessageResponse.builder().build());

        SqsEventConsumerAdapter adapter = new SqsEventConsumerAdapter(
            sqsClient, executeScriptTrait, eventPublisher, objectMapper,
            "queue-url", "results-queue");

        List<Message> messages = adapter.pollMessages();
        assertEquals(1, messages.size());

        adapter.processMessage(messages.get(0));

        verify(eventPublisher).publish(eq("results-queue"), eq("req-3"), anyString());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("SqsEventConsumerAdapter deletes poison messages")
    void sqsConsumerPoison() {
        Message message = Message.builder()
            .messageId("m-2")
            .receiptHandle("rh-2")
            .body("not-json")
            .build();
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
            .thenReturn(DeleteMessageResponse.builder().build());

        SqsEventConsumerAdapter adapter = new SqsEventConsumerAdapter(
            sqsClient, executeScriptTrait, eventPublisher, objectMapper,
            "queue-url", "results-queue");

        adapter.processMessage(message);

        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("SnsEventSenderAdapter publishes to topic ARN")
    void snsSender() {
        when(snsClient.publish(any(PublishRequest.class)))
            .thenReturn(PublishResponse.builder().messageId("mid-2").build());

        SnsEventSenderAdapter adapter = new SnsEventSenderAdapter(snsClient, "default-arn");
        adapter.publish("arn:topic", "key", "payload");

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());
        assertEquals("arn:topic", captor.getValue().topicArn());
        assertEquals("payload", captor.getValue().message());
    }

    @Test
    @DisplayName("EventBridgeEventSenderAdapter puts ScriptExecution event")
    void eventBridgeSender() {
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
            .thenReturn(PutEventsResponse.builder()
                .entries(PutEventsResultEntry.builder().eventId("eid-1").build())
                .build());

        EventBridgeEventSenderAdapter adapter =
            new EventBridgeEventSenderAdapter(eventBridgeClient, "default-bus");
        adapter.publish("my-bus", "key", "{\"ok\":true}");

        ArgumentCaptor<PutEventsRequest> captor = ArgumentCaptor.forClass(PutEventsRequest.class);
        verify(eventBridgeClient).putEvents(captor.capture());
        assertEquals("my-bus", captor.getValue().entries().get(0).eventBusName());
        assertEquals("ScriptExecution", captor.getValue().entries().get(0).detailType());
    }
}
