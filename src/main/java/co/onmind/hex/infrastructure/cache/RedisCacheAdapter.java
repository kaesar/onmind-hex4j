package co.onmind.hex.infrastructure.cache;

import co.onmind.hex.application.ports.out.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisCacheAdapter implements CachePort {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAdapter.class);

    private final StringRedisTemplate redisTemplate;

    public RedisCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String get(String key) {
        String value = redisTemplate.opsForValue().get(key);
        logger.debug("Redis GET key={} hit={}", key, value != null);
        return value;
    }

    @Override
    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
        logger.debug("Redis SET key={} ttl={}s", key, ttl.getSeconds());
    }

    @Override
    public void evict(String key) {
        Boolean deleted = redisTemplate.delete(key);
        logger.debug("Redis EVICT key={} deleted={}", key, deleted);
    }
}
