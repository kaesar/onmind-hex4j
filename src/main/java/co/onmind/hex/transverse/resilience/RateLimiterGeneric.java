package co.onmind.hex.transverse.resilience;

import io.github.resilience4j.ratelimiter.RateLimiter;

import java.util.function.Supplier;

public final class RateLimiterGeneric {

    private RateLimiterGeneric() {
    }

    public static <T> T withRateLimiter(Supplier<T> action, RateLimiter rateLimiter) {
        return rateLimiter.executeSupplier(action);
    }

    public static void withRateLimiter(Runnable action, RateLimiter rateLimiter) {
        rateLimiter.executeRunnable(action);
    }
}
