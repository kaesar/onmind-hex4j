package co.onmind.hex.application.ports.out;

public interface LambdaPort {

    String invoke(String functionName, String payload);

    void invokeAsync(String functionName, String payload);
}
