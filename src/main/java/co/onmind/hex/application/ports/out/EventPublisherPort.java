package co.onmind.hex.application.ports.out;

public interface EventPublisherPort {

    void publish(String topic, String key, String payload);
}
