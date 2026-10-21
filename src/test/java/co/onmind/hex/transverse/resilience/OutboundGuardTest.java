package co.onmind.hex.transverse.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OutboundGuardTest {

    @Test
    @DisplayName("returns the result of the action when every policy allows it")
    void passesThrough() {
        OutboundGuard guard = TestGuards.permissive();

        String result = guard.execute(() -> "ok");

        assertEquals("ok", result);
    }

    @Test
    @DisplayName("rejects with RequestNotPermitted when the rate limit is exceeded")
    void rejectsWhenRateLimitExceeded() {
        OutboundGuard guard = TestGuards.of(100, 2);

        guard.execute(() -> "first");
        guard.execute(() -> "second");

        assertThrows(RequestNotPermitted.class, () -> guard.execute(() -> "third"));
    }

    @Test
    @DisplayName("rejects with BulkheadFullException when too many calls are in flight")
    void rejectsWhenBulkheadIsFull() throws Exception {
        OutboundGuard guard = TestGuards.of(1, 1000);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            Thread blocker = new Thread(() -> guard.execute(() -> {
                started.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }));
            blocker.setDaemon(true);
            blocker.start();

            assertTrue(started.await(5, TimeUnit.SECONDS), "the first call should be in flight");

            assertThrows(BulkheadFullException.class, () -> guard.execute(() -> "never"));
        } finally {
            release.countDown();
        }
    }

    @Test
    @DisplayName("does not trip the circuit breaker when our own bulkhead or rate limiter rejects")
    void rejectionsDoNotTripTheCircuitBreaker() {
        CircuitBreaker circuitBreaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .ignoreExceptions(BulkheadFullException.class, RequestNotPermitted.class)
                .build());

        OutboundGuard guard = new OutboundGuard(
                circuitBreaker,
                Bulkhead.of("test", BulkheadConfig.custom()
                        .maxConcurrentCalls(1)
                        .maxWaitDuration(Duration.ZERO)
                        .build()),
                RateLimiter.of("test", RateLimiterConfig.custom()
                        .limitForPeriod(1)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO)
                        .build()));

        // The first call consumes the only permit of the period and succeeds.
        assertEquals("first", guard.execute(() -> "first"));
        assertThrows(RequestNotPermitted.class, () -> guard.execute(() -> "second"));
        assertThrows(RequestNotPermitted.class, () -> guard.execute(() -> "third"));

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState(),
                "a rate-limit rejection is not a downstream failure and must not open the breaker");
    }

    @Test
    @DisplayName("trips the circuit breaker on real downstream failures")
    void tripsOnDownstreamFailures() {
        CircuitBreaker circuitBreaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .build());

        OutboundGuard guard = new OutboundGuard(
                circuitBreaker,
                Bulkhead.of("test", BulkheadConfig.custom().maxConcurrentCalls(10).build()),
                RateLimiter.of("test", RateLimiterConfig.custom().limitForPeriod(1000).build()));

        assertThrows(IllegalStateException.class, () -> guard.execute(() -> {
            throw new IllegalStateException("downstream exploded");
        }));
        assertThrows(IllegalStateException.class, () -> guard.execute(() -> {
            throw new IllegalStateException("downstream exploded");
        }));

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
    }
}
