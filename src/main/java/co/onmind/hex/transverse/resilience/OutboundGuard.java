package co.onmind.hex.transverse.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;

import java.util.function.Supplier;

/**
 * Composes the resilience policies applied to an outbound call: rate limiter, bulkhead
 * and circuit breaker.
 * <p>
 * The order matters and it is the reason the three policies are composed here instead of
 * being applied one by one at each call site:
 * </p>
 * <ol>
 *   <li><b>Rate limiter</b> (outermost) — rejects excess calls before they occupy any
 *       concurrency slot, and before they can reach the downstream at all.</li>
 *   <li><b>Bulkhead</b> — caps how many calls are <em>in flight</em> against the
 *       downstream. This is the policy that virtual threads make mandatory: without it,
 *       an unbounded number of virtual threads would hit the downstream at once.</li>
 *   <li><b>Circuit breaker</b> (innermost) — only ever observes real downstream
 *       failures. A bulkhead rejection or a rate-limit rejection is not a downstream
 *       failure and must never trip the breaker.</li>
 * </ol>
 *
 * @param circuitBreaker the circuit breaker protecting the downstream
 * @param bulkhead       the bulkhead limiting the concurrent calls
 * @param rateLimiter    the rate limiter limiting the call rate
 */
public record OutboundGuard(
        CircuitBreaker circuitBreaker,
        Bulkhead bulkhead,
        RateLimiter rateLimiter) {

    /**
     * Executes the action under the composed policies.
     *
     * @param action the outbound call
     * @param <T>    the result type
     * @return the result of the action
     */
    public <T> T execute(Supplier<T> action) {
        return RateLimiterGeneric.withRateLimiter(
                () -> BulkheadGeneric.withBulkhead(
                        () -> CircuitBreakerGeneric.withCircuitBreaker(action, circuitBreaker),
                        bulkhead),
                rateLimiter);
    }

    /**
     * Executes the action under the composed policies.
     *
     * @param action the outbound call
     */
    public void execute(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }
}
