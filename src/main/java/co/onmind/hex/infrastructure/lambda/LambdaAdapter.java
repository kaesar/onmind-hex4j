package co.onmind.hex.infrastructure.lambda;

import co.onmind.hex.application.ports.out.LambdaPort;
import co.onmind.hex.transverse.resilience.CircuitBreakerGeneric;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;

@Component
public class LambdaAdapter implements LambdaPort {

    private static final Logger logger = LoggerFactory.getLogger(LambdaAdapter.class);

    private final LambdaClient lambdaClient;
    private final CircuitBreaker circuitBreaker;

    public LambdaAdapter(LambdaClient lambdaClient, CircuitBreaker lambdaCircuitBreaker) {
        this.lambdaClient = lambdaClient;
        this.circuitBreaker = lambdaCircuitBreaker;
    }

    @Override
    public String invoke(String functionName, String payload) {
        logger.debug("Lambda invoke function={}", functionName);
        return CircuitBreakerGeneric.withCircuitBreaker(() -> {
            InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromString(payload != null ? payload : "{}", StandardCharsets.UTF_8))
                .build();
            InvokeResponse response = lambdaClient.invoke(request);
            if (response.functionError() != null && !response.functionError().isBlank()) {
                throw new RuntimeException("Lambda function error: " + response.functionError()
                    + " payload=" + response.payload().asUtf8String());
            }
            String result = response.payload() != null ? response.payload().asUtf8String() : null;
            logger.debug("Lambda invoke OK function={}", functionName);
            return result;
        }, circuitBreaker);
    }

    @Override
    public void invokeAsync(String functionName, String payload) {
        logger.debug("Lambda invokeAsync function={}", functionName);
        CircuitBreakerGeneric.withCircuitBreaker(() -> {
            InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.EVENT)
                .payload(SdkBytes.fromString(payload != null ? payload : "{}", StandardCharsets.UTF_8))
                .build();
            lambdaClient.invoke(request);
            logger.debug("Lambda invokeAsync accepted function={}", functionName);
        }, circuitBreaker);
    }
}
