package co.onmind.hex.transverse.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import java.util.function.Supplier;

public final class CircuitBreakerGeneric {

    private CircuitBreakerGeneric() {
    }

    public static <T> T withCircuitBreaker(Supplier<T> action, CircuitBreaker circuitBreaker) {
        return circuitBreaker.executeSupplier(action);
    }

    public static void withCircuitBreaker(Runnable action, CircuitBreaker circuitBreaker) {
        circuitBreaker.executeRunnable(action);
    }
}
