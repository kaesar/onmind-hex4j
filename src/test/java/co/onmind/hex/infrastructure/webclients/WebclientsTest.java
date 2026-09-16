package co.onmind.hex.infrastructure.webclients;

import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebclientsTest {

    @Mock private AbcWebClient webClient;
    @Mock private CachePort cachePort;

    private CircuitBreaker circuitBreaker;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        circuitBreaker = CircuitBreaker.ofDefaults("test");
        objectMapper = new JsonMapper();
    }

    @Test
    @DisplayName("AbcAdapter delegates sheet and exec through circuit breaker")
    void abcAdapter() {
        AbcResponse sheetResponse = new AbcResponse(true, 200, "OK", 1, List.of());
        AbcResponse execResponse = new AbcResponse(true, 200, "OK", 0, null);
        when(webClient.sheet("s", "f", "c")).thenReturn(sheetResponse);
        when(webClient.ask(any(AbcRequest.class))).thenReturn(execResponse);

        AbcAdapter adapter = new AbcAdapter(webClient, circuitBreaker);

        assertEquals(1, adapter.sheet("s", "f", "c").total());
        AbcRequest request = AbcRequest.builder().what("find").from("f").some("c").build();
        assertTrue(adapter.exec(request).ok());
    }

    @Test
    @DisplayName("CachedAbcAdapter returns cached response on HIT")
    void cachedHit() throws Exception {
        AbcResponse cached = new AbcResponse(true, 200, "OK", 5, List.of());
        when(cachePort.get(anyString())).thenReturn(objectMapper.writeValueAsString(cached));

        CachedAbcAdapter adapter = new CachedAbcAdapter(
            mock(AbcAdapter.class), cachePort, objectMapper, Duration.ofSeconds(300));

        AbcResponse result = adapter.sheet("s", "f", "c");

        assertEquals(5, result.total());
        verifyNoInteractions(webClient);
    }

    @Test
    @DisplayName("CachedAbcAdapter fetches and caches on MISS")
    void cachedMiss() {
        when(cachePort.get(anyString())).thenReturn(null);
        AbcResponse fresh = new AbcResponse(true, 200, "OK", 2, List.of());
        AbcAdapter delegate = mock(AbcAdapter.class);
        when(delegate.sheet("s", "f", "c")).thenReturn(fresh);

        CachedAbcAdapter adapter = new CachedAbcAdapter(
            delegate, cachePort, objectMapper, Duration.ofSeconds(300));

        AbcResponse result = adapter.sheet("s", "f", "c");

        assertEquals(2, result.total());
        verify(cachePort).set(anyString(), anyString(), eq(Duration.ofSeconds(300)));
    }

    @Test
    @DisplayName("CachedAbcAdapter falls back to delegate on cache error")
    void cachedError() {
        when(cachePort.get(anyString())).thenThrow(new RuntimeException("redis down"));
        AbcResponse fresh = new AbcResponse(true, 200, "OK", 3, List.of());
        AbcAdapter delegate = mock(AbcAdapter.class);
        when(delegate.sheet("s", "f", "c")).thenReturn(fresh);

        CachedAbcAdapter adapter = new CachedAbcAdapter(
            delegate, cachePort, objectMapper, Duration.ofSeconds(300));

        assertEquals(3, adapter.sheet("s", "f", "c").total());
    }

    @Test
    @DisplayName("CachedAbcAdapter.exec always delegates without cache")
    void execDelegates() {
        AbcAdapter delegate = mock(AbcAdapter.class);
        AbcResponse response = new AbcResponse(true, 200, "OK", 0, null);
        AbcRequest request = AbcRequest.builder().what("insert").from("f").some("c").build();
        when(delegate.exec(request)).thenReturn(response);

        CachedAbcAdapter adapter = new CachedAbcAdapter(
            delegate, cachePort, objectMapper, Duration.ofSeconds(300));

        assertTrue(adapter.exec(request).ok());
        verifyNoInteractions(cachePort);
    }
}
