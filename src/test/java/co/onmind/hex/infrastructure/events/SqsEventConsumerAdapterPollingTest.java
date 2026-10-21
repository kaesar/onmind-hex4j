package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsEventConsumerAdapterPollingTest {

    @Mock private SqsClient sqsClient;
    @Mock private ExecuteScriptTrait executeScriptTrait;
    @Mock private EventPublisherPort eventPublisher;

    private final ObjectMapper objectMapper = new JsonMapper();

    private SqsEventConsumerAdapter adapter() {
        return new SqsEventConsumerAdapter(
                sqsClient, executeScriptTrait, eventPublisher, objectMapper,
                "queue-url", "results-queue", 120);
    }

    @Test
    @DisplayName("the receive request uses the configured visibility timeout")
    void usesConfiguredVisibilityTimeout() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().build());

        adapter().pollMessages();

        ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(captor.capture());

        // The visibility timeout must outlast the maximum script execution time
        // (app.scripts.timeout-seconds, 30s), otherwise the message reappears while
        // it is still being processed and the script runs twice in parallel.
        assertEquals(120, captor.getValue().visibilityTimeout());
        assertTrue(captor.getValue().visibilityTimeout() > 30,
                "visibilityTimeout must be greater than the script execution timeout");
    }

    @Test
    @DisplayName("pollAndProcess consumes every message of the batch")
    void pollAndProcessConsumesTheBatch() {
        Message first = Message.builder()
                .messageId("m-1")
                .receiptHandle("rh-1")
                .body("{\"script\":\"hello.js\",\"correlationId\":\"req-1\"}")
                .build();
        Message second = Message.builder()
                .messageId("m-2")
                .receiptHandle("rh-2")
                .body("{\"script\":\"hello.js\",\"correlationId\":\"req-2\"}")
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(first, second).build());
        when(executeScriptTrait.executeScript("hello.js"))
                .thenReturn(new ScriptResultResponseDto("hi", "", null));
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(software.amazon.awssdk.services.sqs.model.DeleteMessageResponse.builder().build());

        adapter().pollAndProcess();

        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
        verify(eventPublisher).publish(eq("results-queue"), eq("req-1"), anyString());
        verify(eventPublisher).publish(eq("results-queue"), eq("req-2"), anyString());
    }

    @Test
    @DisplayName("pollAndProcess survives a failing message and keeps consuming the rest")
    void pollAndProcessSurvivesAFailingMessage() {
        Message poison = Message.builder()
                .messageId("m-1")
                .receiptHandle("rh-1")
                .body("not-json")
                .build();
        Message good = Message.builder()
                .messageId("m-2")
                .receiptHandle("rh-2")
                .body("{\"script\":\"hello.js\",\"correlationId\":\"req-2\"}")
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(poison, good).build());
        when(executeScriptTrait.executeScript("hello.js"))
                .thenReturn(new ScriptResultResponseDto("hi", "", null));
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(software.amazon.awssdk.services.sqs.model.DeleteMessageResponse.builder().build());

        assertDoesNotThrow(() -> adapter().pollAndProcess());

        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
        verify(eventPublisher).publish(eq("results-queue"), eq("req-2"), anyString());
    }

    @Test
    @DisplayName("pollAndProcess survives a failing poll")
    void pollAndProcessSurvivesAFailingPoll() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(new RuntimeException("queue unreachable"));

        assertDoesNotThrow(() -> adapter().pollAndProcess());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("a failing script publishes the error and consumes the message")
    void failingScriptConsumesTheMessage() {
        Message message = Message.builder()
                .messageId("m-1")
                .receiptHandle("rh-1")
                .body("{\"script\":\"boom.js\",\"correlationId\":\"req-9\"}")
                .build();
        when(executeScriptTrait.executeScript("boom.js"))
                .thenThrow(new RuntimeException("script exploded"));
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(software.amazon.awssdk.services.sqs.model.DeleteMessageResponse.builder().build());

        adapter().processMessage(message);

        // The error is published...
        verify(eventPublisher).publish(eq("results-queue"), eq("req-9"), anyString());
        // ...and the message is consumed, so it does not come back after the
        // visibility timeout and run the script again, forever.
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("a failure to publish the result leaves the message in the queue for a retry")
    void failingPublicationKeepsTheMessage() {
        Message message = Message.builder()
                .messageId("m-1")
                .receiptHandle("rh-1")
                .body("{\"script\":\"hello.js\",\"correlationId\":\"req-10\"}")
                .build();
        when(executeScriptTrait.executeScript("hello.js"))
                .thenReturn(new ScriptResultResponseDto("hi", "", null));
        doThrow(new RuntimeException("broker down"))
                .when(eventPublisher).publish(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class, () -> adapter().processMessage(message));

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }
}
