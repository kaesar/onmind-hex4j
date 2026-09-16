package co.onmind.hex.transverse;

import co.onmind.hex.infrastructure.configuration.RestClientConfiguration.ExternalServiceException;
import co.onmind.hex.infrastructure.webclients.dto.AbcToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;

/**
 * Generic blocking utility over {@link RestClient} for external HTTP calls.
 *
 * <p>Same two flavors as before: plain and with {@link AbcToken} auth header.
 * Calls are imperative end-to-end (no reactive types) and retry transient
 * failures (5xx, connection/timeout/IO errors) up to 3 attempts with
 * exponential backoff (500ms, capped at 5s).</p>
 */
public class RestClientGeneric {

    private static final Logger logger = LoggerFactory.getLogger(RestClientGeneric.class);
    private static final int MAX_ATTEMPTS = 3;

    private final RestClient restClient;

    public RestClientGeneric(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T get(String uri, Class<T> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making GET request to: {}", uri);
            T result = restClient.get()
                .uri(uri)
                .retrieve()
                .body(responseType);
            logger.debug("GET request successful: {}", uri);
            return result;
        }, "GET " + uri);
    }

    public <T> T get(String uri, Class<T> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(uri);
            applyAuth(spec, auth);
            T result = spec.retrieve().body(responseType);
            logger.debug("GET request successful: {}", uri);
            return result;
        }, "GET " + uri);
    }

    public <T> List<T> getMany(String uri, Class<T[]> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making GET request for collection to: {}", uri);
            T[] result = restClient.get()
                .uri(uri)
                .retrieve()
                .body(responseType);
            logger.debug("GET collection request successful: {}", uri);
            return result != null ? Arrays.asList(result) : List.of();
        }, "GET " + uri);
    }

    public <T> List<T> getMany(String uri, Class<T[]> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(uri);
            applyAuth(spec, auth);
            T[] result = spec.retrieve().body(responseType);
            logger.debug("GET collection request successful: {}", uri);
            return result != null ? Arrays.asList(result) : List.of();
        }, "GET " + uri);
    }

    public <T, R> R post(String uri, T body, Class<R> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making POST request to: {}", uri);
            R result = restClient.post()
                .uri(uri)
                .body(body)
                .retrieve()
                .body(responseType);
            logger.debug("POST request successful: {}", uri);
            return result;
        }, "POST " + uri);
    }

    public <T, R> R post(String uri, T body, Class<R> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            RestClient.RequestBodySpec spec = restClient.post().uri(uri);
            applyAuth(spec, auth);
            R result = spec.body(body).retrieve().body(responseType);
            logger.debug("POST request successful: {}", uri);
            return result;
        }, "POST " + uri);
    }

    public <T> void postVoid(String uri, T body) {
        executeWithRetry(() -> {
            logger.debug("Making POST request to: {}", uri);
            restClient.post()
                .uri(uri)
                .body(body)
                .retrieve()
                .toBodilessEntity();
            logger.debug("POST request successful: {}", uri);
            return null;
        }, "POST " + uri);
    }

    public <T, R> R put(String uri, T body, Class<R> responseType) {
        return executeWithRetry(() -> {
            logger.debug("Making PUT request to: {}", uri);
            R result = restClient.put()
                .uri(uri)
                .body(body)
                .retrieve()
                .body(responseType);
            logger.debug("PUT request successful: {}", uri);
            return result;
        }, "PUT " + uri);
    }

    public <T, R> R put(String uri, T body, Class<R> responseType, AbcToken auth) {
        return executeWithRetry(() -> {
            RestClient.RequestBodySpec spec = restClient.put().uri(uri);
            applyAuth(spec, auth);
            R result = spec.body(body).retrieve().body(responseType);
            logger.debug("PUT request successful: {}", uri);
            return result;
        }, "PUT " + uri);
    }

    public void delete(String uri) {
        executeWithRetry(() -> {
            logger.debug("Making DELETE request to: {}", uri);
            restClient.delete()
                .uri(uri)
                .retrieve()
                .toBodilessEntity();
            logger.debug("DELETE request successful: {}", uri);
            return null;
        }, "DELETE " + uri);
    }

    public void delete(String uri, AbcToken auth) {
        executeWithRetry(() -> {
            RestClient.RequestHeadersSpec<?> spec = restClient.delete().uri(uri);
            applyAuth(spec, auth);
            spec.retrieve().toBodilessEntity();
            logger.debug("DELETE request successful: {}", uri);
            return null;
        }, "DELETE " + uri);
    }

    private void applyAuth(RestClient.RequestHeadersSpec<?> spec, AbcToken auth) {
        String hdr = auth.toHeaderValue();
        if (!hdr.isEmpty()) {
            spec.header(HttpHeaders.AUTHORIZATION, hdr);
        }
    }

    private <T> T executeWithRetry(java.util.function.Supplier<T> action, String operation) {
        int attempt = 0;
        long backoffMs = 500;
        while (true) {
            attempt++;
            try {
                return action.get();
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                String body = e.getResponseBodyAsString();
                if (body == null || body.isBlank()) {
                    body = "Unknown error";
                }
                String errorMessage = "HTTP %d error: %s".formatted(status, body);
                logger.error("External service error: {}", errorMessage);
                ExternalServiceException failure = new ExternalServiceException(errorMessage, status);
                if (!isRetryable(failure) || attempt >= MAX_ATTEMPTS) {
                    logger.error("{} failed: {} - Error: {}", operation, attempt, failure.getMessage());
                    throw failure;
                }
                logger.warn("{} attempt {}/{} failed, retrying in {}ms: {}",
                    operation, attempt, MAX_ATTEMPTS, backoffMs, failure.getMessage());
                sleep(backoffMs, operation);
                backoffMs = Math.min(backoffMs * 2, 5000);
            } catch (Exception e) {
                if (!isRetryable(e) || attempt >= MAX_ATTEMPTS) {
                    logger.error("{} failed: {} - Error: {}", operation, attempt, e.getMessage());
                    throw e;
                }
                logger.warn("{} attempt {}/{} failed, retrying in {}ms: {}",
                    operation, attempt, MAX_ATTEMPTS, backoffMs, e.getMessage());
                sleep(backoffMs, operation);
                backoffMs = Math.min(backoffMs * 2, 5000);
            }
        }
    }

    private void sleep(long backoffMs, String operation) {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted for " + operation, ie);
        }
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof ExternalServiceException ese) {
            return ese.getStatusCode() >= 500;
        }
        for (Throwable current = e; current != null; current = current.getCause()) {
            if (current instanceof java.net.ConnectException
                || current instanceof java.util.concurrent.TimeoutException
                || current instanceof java.io.IOException) {
                return true;
            }
        }
        return false;
    }
}
