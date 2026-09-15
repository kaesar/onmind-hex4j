package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.application.ports.out.EmailPort;
import co.onmind.hex.application.ports.out.EventPublisherPort;
import co.onmind.hex.application.ports.out.LambdaPort;
import co.onmind.hex.application.ports.out.StorePort;
import co.onmind.hex.domain.models.StoreItem;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScriptServicesFacadeTest {

    @Mock private AbcPort abcPort;
    @Mock private EventPublisherPort eventPublisher;
    @Mock private LambdaPort lambdaPort;
    @Mock private StorePort storePort;
    @Mock private EmailPort emailPort;
    @Mock private CachePort cachePort;

    private ScriptServicesFacade facade;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        facade = new ScriptServicesFacade(
            abcPort,
            mock(ObjectProvider.class),
            lambdaPort, storePort, emailPort, cachePort
        );
    }

    @Test
    @DisplayName("abcSheet delegates to AbcPort.sheet")
    void abcSheetDelegatesToPort() {
        AbcResponse mockResponse = new AbcResponse(true, 200, "ok", 1, List.of());
        when(abcPort.sheet("cols", "table1", "sheet")).thenReturn(mockResponse);

        AbcResponse result = facade.abcSheet("cols", "table1", "sheet");
        assertEquals(true, result.ok());
        assertEquals(200, result.status());
    }

    @Test
    @DisplayName("abcExec builds AbcRequest and delegates to AbcPort.exec")
    void abcExecDelegatesToPort() {
        AbcResponse mockResponse = new AbcResponse(true, 200, "ok", 0, null);
        when(abcPort.exec(any(AbcRequest.class))).thenReturn(mockResponse);

        AbcResponse result = facade.abcExec("insert", "table1", "sheet", null, "{\"key\":\"val\"}");
        assertEquals(true, result.ok());
        verify(abcPort).exec(argThat(req -> "insert".equals(req.what()) && "table1".equals(req.from())));
    }

    @Test
    @DisplayName("publish delegates to EventPublisherPort")
    void publishDelegates() {
        ObjectProvider<EventPublisherPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(eventPublisher);
        facade = new ScriptServicesFacade(abcPort, provider, lambdaPort, storePort, emailPort, cachePort);

        facade.publish("my-topic", "my-key", "payload");
        verify(eventPublisher).publish("my-topic", "my-key", "payload");
    }

    @Test
    @DisplayName("publish throws when no event publisher available")
    void publishThrowsWhenNoPublisher() {
        ObjectProvider<EventPublisherPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        facade = new ScriptServicesFacade(abcPort, provider, lambdaPort, storePort, emailPort, cachePort);

        assertThrows(UnsupportedOperationException.class,
            () -> facade.publish("topic", "key", "payload"));
    }

    @Test
    @DisplayName("invoke delegates to LambdaPort")
    void invokeDelegates() {
        when(lambdaPort.invoke("my-func", "{\"action\":\"ping\"}"))
            .thenReturn("{\"response\":\"pong\"}");

        String result = facade.invoke("my-func", "{\"action\":\"ping\"}");
        assertEquals("{\"response\":\"pong\"}", result);
    }

    @Test
    @DisplayName("invokeAsync delegates to LambdaPort.invokeAsync")
    void invokeAsyncDelegates() {
        facade.invokeAsync("my-func", "{\"action\":\"fire\"}");
        verify(lambdaPort).invokeAsync("my-func", "{\"action\":\"fire\"}");
    }

    @Test
    @DisplayName("listItems delegates to StorePort")
    void listItemsDelegates() {
        List<StoreItem> mockItems = List.of(
            new StoreItem("file1.txt", 100L, LocalDateTime.now(), "etag1"),
            new StoreItem("file2.txt", 200L, LocalDateTime.now(), "etag2")
        );
        when(storePort.listItems("my-bucket")).thenReturn(mockItems);

        List<StoreItem> result = facade.listItems("my-bucket");
        assertEquals(2, result.size());
        assertEquals("file1.txt", result.get(0).key());
    }

    @Test
    @DisplayName("sendEmail delegates to EmailPort")
    void sendEmailDelegates() {
        facade.sendEmail("user@test.com", "subject", "body");
        verify(emailPort).send("user@test.com", "subject", "body");
    }

    @Test
    @DisplayName("cacheGet delegates to CachePort")
    void cacheGetDelegates() {
        when(cachePort.get("my-key")).thenReturn("my-value");

        String result = facade.cacheGet("my-key");
        assertEquals("my-value", result);
    }

    @Test
    @DisplayName("cacheSet delegates to CachePort with 5-min TTL")
    void cacheSetDelegates() {
        facade.cacheSet("my-key", "my-value");
        verify(cachePort).set(eq("my-key"), eq("my-value"), eq(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("cacheEvict delegates to CachePort")
    void cacheEvictDelegates() {
        facade.cacheEvict("my-key");
        verify(cachePort).evict("my-key");
    }
}
