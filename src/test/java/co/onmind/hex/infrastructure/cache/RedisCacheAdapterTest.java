package co.onmind.hex.infrastructure.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("get delegates to Redis value operations")
    void get() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("k")).thenReturn("v");

        RedisCacheAdapter adapter = new RedisCacheAdapter(redisTemplate);

        assertEquals("v", adapter.get("k"));
    }

    @Test
    @DisplayName("set delegates with TTL")
    void set() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisCacheAdapter adapter = new RedisCacheAdapter(redisTemplate);
        adapter.set("k", "v", Duration.ofSeconds(60));

        verify(valueOperations).set("k", "v", Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("evict deletes key")
    void evict() {
        RedisCacheAdapter adapter = new RedisCacheAdapter(redisTemplate);
        adapter.evict("k");

        verify(redisTemplate).delete("k");
    }
}
