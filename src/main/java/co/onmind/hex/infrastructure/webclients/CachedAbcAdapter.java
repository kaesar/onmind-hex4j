package co.onmind.hex.infrastructure.webclients;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class CachedAbcAdapter implements AbcPort {

    private static final Logger logger = LoggerFactory.getLogger(CachedAbcAdapter.class);

    private final AbcPort delegate;
    private final CachePort cachePort;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public CachedAbcAdapter(AbcPort delegate, CachePort cachePort,
                            ObjectMapper objectMapper, Duration ttl) {
        this.delegate = delegate;
        this.cachePort = cachePort;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public AbcResponse sheet(String show, String from, String some) {
        String key = cacheKey(show, from, some);

        try {
            String cached = cachePort.get(key);
            if (cached != null && !cached.isBlank()) {
                try {
                    AbcResponse cachedResponse = objectMapper.readValue(cached, AbcResponse.class);
                    logger.debug("Cache HIT key={}", key);
                    return cachedResponse;
                } catch (JsonProcessingException e) {
                    logger.warn("Cache value for key={} could not be deserialized, ignoring", key);
                }
            }
        } catch (Exception e) {
            logger.debug("Cache read error for key={}, falling back to delegate", key, e);
        }

        return fetchAndCache(show, from, some, key);
    }

    @Override
    public AbcResponse exec(AbcRequest request) {
        return delegate.exec(request);
    }

    private AbcResponse fetchAndCache(String show, String from, String some, String key) {
        AbcResponse response = delegate.sheet(show, from, some);
        String json = serialize(response, key);
        if (json == null) {
            return response;
        }
        try {
            cachePort.set(key, json, ttl);
        } catch (Exception e) {
            logger.debug("Cache write error for key={}, returning uncached", key, e);
        }
        return response;
    }

    private String serialize(AbcResponse response, String key) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.warn("Unable to serialize AbcResponse for cache at key={}", key, e);
            return null;
        }
    }

    private String cacheKey(String show, String from, String some) {
        return "abc:sheet:%s:%s:%s".formatted(
                show != null ? show : "",
                from != null ? from : "",
                some != null ? some : ""
        );
    }
}
