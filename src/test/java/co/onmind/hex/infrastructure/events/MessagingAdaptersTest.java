package co.onmind.hex.infrastructure.events;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingAdaptersTest {

    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ExecuteScriptTrait executeScriptTrait;
    @Mock private EventPublisherPort eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("KafkaEventPublisherAdapter sends via KafkaTemplate")
    void kafkaPublisher() {
        KafkaEventPublisherAdapter adapter = new KafkaEventPublisherAdapter(kafkaTemplate);
        adapter.publish("topic", "key", "payload");

        verify(kafkaTemplate).send("topic", "key", "payload");
    }

    @Test
    @DisplayName("KafkaEventConsumerAdapter executes script and publishes result")
    void kafkaConsumer() throws Exception {
        ScriptResultResponseDto result = new ScriptResultResponseDto("hi", "", null);
        when(executeScriptTrait.executeScript("hello.js")).thenReturn(result);

        KafkaEventConsumerAdapter adapter = new KafkaEventConsumerAdapter(
            executeScriptTrait, eventPublisher, objectMapper, "results-topic");
        adapter.onScriptCommand("{\"script\":\"hello.js\",\"correlationId\":\"req-1\"}");

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher).publish(eq("results-topic"), eq("req-1"), payload.capture());
        assertTrue(payload.getValue().contains("req-1"));
    }

    @Test
    @DisplayName("KafkaEventConsumerAdapter ignores malformed messages")
    void kafkaConsumerMalformed() {
        KafkaEventConsumerAdapter adapter = new KafkaEventConsumerAdapter(
            executeScriptTrait, eventPublisher, objectMapper, "results-topic");
        adapter.onScriptCommand("not-json");

        verifyNoInteractions(executeScriptTrait, eventPublisher);
    }

    @Test
    @DisplayName("KafkaEventConsumerAdapter publishes error on script failure")
    void kafkaConsumerFailure() {
        when(executeScriptTrait.executeScript(anyString()))
            .thenThrow(new RuntimeException("boom"));

        KafkaEventConsumerAdapter adapter = new KafkaEventConsumerAdapter(
            executeScriptTrait, eventPublisher, objectMapper, "results-topic");
        adapter.onScriptCommand("{\"script\":\"bad.js\",\"correlationId\":\"req-9\"}");

        verify(eventPublisher).publish(eq("results-topic"), eq("req-9"), anyString());
    }

    @Test
    @DisplayName("RabbitMQEventPublisherAdapter sends via RabbitTemplate")
    void rabbitPublisher() {
        RabbitMQEventPublisherAdapter adapter =
            new RabbitMQEventPublisherAdapter(rabbitTemplate, "default-exchange");
        adapter.publish("my-exchange", "my-key", "payload");

        verify(rabbitTemplate).convertAndSend("my-exchange", "my-key", "payload");
    }

    @Test
    @DisplayName("RabbitMQEventPublisherAdapter falls back to default exchange")
    void rabbitPublisherDefault() {
        RabbitMQEventPublisherAdapter adapter =
            new RabbitMQEventPublisherAdapter(rabbitTemplate, "default-exchange");
        adapter.publish(null, null, "payload");

        verify(rabbitTemplate).convertAndSend("default-exchange", "", "payload");
    }

    @Test
    @DisplayName("RabbitMQEventConsumerAdapter executes script and publishes result")
    void rabbitConsumer() {
        ScriptResultResponseDto result = new ScriptResultResponseDto("hi", "", null);
        when(executeScriptTrait.executeScript("hello.js")).thenReturn(result);

        RabbitMQEventConsumerAdapter adapter = new RabbitMQEventConsumerAdapter(
            executeScriptTrait, eventPublisher, objectMapper, "results-exchange");
        adapter.onScriptCommand("{\"script\":\"hello.js\",\"correlationId\":\"req-2\"}");

        verify(eventPublisher).publish(eq("results-exchange"), eq("req-2"), anyString());
    }
}
