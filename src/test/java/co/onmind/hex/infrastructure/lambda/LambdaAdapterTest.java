package co.onmind.hex.infrastructure.lambda;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LambdaAdapterTest {

    @Mock private LambdaClient lambdaClient;

    private LambdaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LambdaAdapter(lambdaClient, CircuitBreaker.ofDefaults("test"));
    }

    @Test
    @DisplayName("invoke returns payload string")
    void invokeOk() {
        InvokeResponse response = InvokeResponse.builder()
            .payload(SdkBytes.fromUtf8String("{\"ok\":true}"))
            .build();
        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(response);

        String result = adapter.invoke("my-fn", "{\"a\":1}");

        assertEquals("{\"ok\":true}", result);
        ArgumentCaptor<InvokeRequest> captor = ArgumentCaptor.forClass(InvokeRequest.class);
        verify(lambdaClient).invoke(captor.capture());
        assertEquals("my-fn", captor.getValue().functionName());
    }

    @Test
    @DisplayName("invoke throws on function error")
    void invokeFunctionError() {
        InvokeResponse response = InvokeResponse.builder()
            .functionError("Unhandled")
            .payload(SdkBytes.fromUtf8String("{}"))
            .build();
        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(response);

        assertThrows(RuntimeException.class, () -> adapter.invoke("my-fn", "{}"));
    }

    @Test
    @DisplayName("invokeAsync uses EVENT invocation type")
    void invokeAsyncOk() {
        when(lambdaClient.invoke(any(InvokeRequest.class)))
            .thenReturn(InvokeResponse.builder().statusCode(202).build());

        adapter.invokeAsync("my-fn", "{}");

        ArgumentCaptor<InvokeRequest> captor = ArgumentCaptor.forClass(InvokeRequest.class);
        verify(lambdaClient).invoke(captor.capture());
        assertEquals(
            software.amazon.awssdk.services.lambda.model.InvocationType.EVENT,
            captor.getValue().invocationType());
    }
}
