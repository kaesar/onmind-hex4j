package co.onmind.hex.transverse;

import co.onmind.hex.infrastructure.configuration.WebClientConfiguration.ExternalServiceException;
import co.onmind.hex.infrastructure.webclients.dto.AbcToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Generic blocking utility over {@link WebClient} for external HTTP calls.
 *
 * <p>Same two flavors as hex4w: plain and with {@link AbcToken} auth header.
 * Calls block with a 30s timeout and retry transient failures (5xx,
 * connection/timeout/IO errors) up to 3 attempts with exponential backoff
 * (500ms, capped at 5s) — mirroring the reactive retry spec from hex4w.
 */
public class WebClientGeneric {

    private static final Logger logger = LoggerFactory.getLogger(WebClientGeneric.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 3;

    private final WebClient webClient;

    public WebClientGeneric(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T get(String uri, Class<T> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making GET request to: {}", uri);
            T result = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .block(TIMEOUT);
            logger.debug("GET request successful: {}", uri);
            return result;
        }, "GET " + uri);
    }

    public <T> T get(String uri, Class<T> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            var spec = webClient.get().uri(uri);
            String hdr = auth.toHeaderValue();
            if (!hdr.isEmpty()) spec.header(HttpHeaders.AUTHORIZATION, hdr);
            T result = spec.retrieve().bodyToMono(responseType).block(TIMEOUT);
            logger.debug("GET request successful: {}", uri);
            return result;
        }, "GET " + uri);
    }

    public <T> List<T> getMany(String uri, Class<T[]> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making GET request for collection to: {}", uri);
            T[] result = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .block(TIMEOUT);
            logger.debug("GET collection request successful: {}", uri);
            return result != null ? Arrays.asList(result) : List.of();
        }, "GET " + uri);
    }

    public <T> List<T> getMany(String uri, Class<T[]> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            var spec = webClient.get().uri(uri);
            String hdr = auth.toHeaderValue();
            if (!hdr.isEmpty()) spec.header(HttpHeaders.AUTHORIZATION, hdr);
            T[] result = spec.retrieve().bodyToMono(responseType).block(TIMEOUT);
            logger.debug("GET collection request successful: {}", uri);
            return result != null ? Arrays.asList(result) : List.of();
        }, "GET " + uri);
    }

    public <T, R> R post(String uri, T body, Class<R> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making POST request to: {}", uri);
            R result = webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block(TIMEOUT);
            logger.debug("POST request successful: {}", uri);
            return result;
        }, "POST " + uri);
    }

    public <T, R> R post(String uri, T body, Class<R> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            var spec = webClient.post().uri(uri);
            String hdr = auth.toHeaderValue();
            if (!hdr.isEmpty()) spec.header(HttpHeaders.AUTHORIZATION, hdr);
            R result = spec.bodyValue(body).retrieve().bodyToMono(responseType).block(TIMEOUT);
            logger.debug("POST request successful: {}", uri);
            return result;
        }, "POST " + uri);
    }

    public <T> void postVoid(String uri, T body) {
        executeWithRetry(() -> {
            logger.debug("Making POST request to: {}", uri);
            webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .block(TIMEOUT);
            logger.debug("POST request successful: {}", uri);
            return null;
        }, "POST " + uri);
    }

    public <T, R> R put(String uri, T body, Class<R> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making PUT request to: {}", uri);
            R result = webClient.put()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block(TIMEOUT);
            logger.debug("PUT request successful: {}", uri);
            return result;
        }, "PUT " + uri);
    }

    public <T, R> R put(String uri, T body, Class<R> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            var spec = webClient.put().uri(uri);
            String hdr = auth.toHeaderValue();
            if (!hdr.isEmpty()) spec.header(HttpHeaders.AUTHORIZATION, hdr);
            R result = spec.bodyValue(body).retrieve().bodyToMono(responseType).block(TIMEOUT);
            logger.debug("PUT request successful: {}", uri);
            return result;
        }, "PUT " + uri);
    }

    public void delete(String uri) {
        executeWithRetry(() -> {
            logger.debug("Making DELETE request to: {}", uri);
            webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(Void.class)
                .block(TIMEOUT);
            logger.debug("DELETE request successful: {}", uri);
            return null;
        }, "DELETE " + uri);
    }

    public void delete(String uri, AbcToken auth) {
        executeWithRetry(() -> {
            var spec = webClient.delete().uri(uri);
            String hdr = auth.toHeaderValue();
            if (!hdr.isEmpty()) spec.header(HttpHeaders.AUTHORIZATION, hdr);
            spec.retrieve().bodyToMono(Void.class).block(TIMEOUT);
            logger.debug("DELETE request successful: {}", uri);
            return null;
        }, "DELETE " + uri);
    }

    private <T> T executeWithRetry(java.util.function.Supplier<T> action, String operation) {
        int attempt = 0;
        long backoffMs = 500;
        while (true) {
            attempt++;
            try {
                return action.get();
            } catch (Exception e) {
                if (!isRetryable(e) || attempt >= MAX_ATTEMPTS) {
                    logger.error("{} failed: {} - Error: {}", operation, attempt, e.getMessage());
                    throw e;
                }
                logger.warn("{} attempt {}/{} failed, retrying in {}ms: {}",
                    operation, attempt, MAX_ATTEMPTS, backoffMs, e.getMessage());
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted for " + operation, ie);
                }
                backoffMs = Math.min(backoffMs * 2, 5000);
            }
        }
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof ExternalServiceException ese) {
            return ese.getStatusCode() >= 500;
        }
        return e instanceof java.net.ConnectException
            || e instanceof java.util.concurrent.TimeoutException
            || e instanceof java.io.IOException;
    }
}
