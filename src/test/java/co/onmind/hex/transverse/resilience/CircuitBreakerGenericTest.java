package co.onmind.hex.transverse.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerGenericTest {

    @Test
    @DisplayName("withCircuitBreaker returns supplier result")
    void supplier() {
        CircuitBreaker breaker = CircuitBreaker.ofDefaults("test");
        String result = CircuitBreakerGeneric.withCircuitBreaker(() -> "ok", breaker);
        assertEquals("ok", result);
    }

    @Test
    @DisplayName("withCircuitBreaker propagates supplier failure")
    void supplierFailure() {
        CircuitBreaker breaker = CircuitBreaker.ofDefaults("test");
        assertThrows(RuntimeException.class, () ->
            CircuitBreakerGeneric.withCircuitBreaker(() -> {
                throw new RuntimeException("boom");
            }, breaker));
    }

    @Test
    @DisplayName("withCircuitBreaker runs runnable")
    void runnable() {
        CircuitBreaker breaker = CircuitBreaker.ofDefaults("test");
        boolean[] ran = {false};
        CircuitBreakerGeneric.withCircuitBreaker(() -> ran[0] = true, breaker);
        assertTrue(ran[0]);
    }
}
