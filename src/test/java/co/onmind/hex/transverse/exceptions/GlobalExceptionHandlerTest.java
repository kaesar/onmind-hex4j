package co.onmind.hex.transverse.exceptions;

import co.onmind.hex.infrastructure.controllers.ErrorResponse;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("maps a bulkhead rejection to 503 Service Unavailable")
    void bulkheadRejection() {
        when(request.getRequestURI()).thenReturn("/api/v1/xdb/sheet");
        Bulkhead bulkhead = Bulkhead.of("test", BulkheadConfig.custom().maxConcurrentCalls(1).build());

        ResponseEntity<ErrorResponse> response = handler.handleOverloaded(
                BulkheadFullException.createBulkheadFullException(bulkhead), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().code());
    }

    @Test
    @DisplayName("maps a rate-limit rejection to 503 Service Unavailable")
    void rateLimiterRejection() {
        when(request.getRequestURI()).thenReturn("/api/v1/xdb/sheet");
        RateLimiter rateLimiter = RateLimiter.of("test", RateLimiterConfig.custom().limitForPeriod(1).build());

        ResponseEntity<ErrorResponse> response = handler.handleOverloaded(
                RequestNotPermitted.createRequestNotPermitted(rateLimiter), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().code());
    }
}
