package co.onmind.hex.application.ports.out;

import java.time.Duration;

public interface CachePort {

    String get(String key);

    void set(String key, String value, Duration ttl);

    void evict(String key);
}
