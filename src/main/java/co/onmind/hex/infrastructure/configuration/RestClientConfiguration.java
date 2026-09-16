package co.onmind.hex.infrastructure.configuration;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.infrastructure.webclients.AbcAdapter;
import co.onmind.hex.infrastructure.webclients.AbcWebClient;
import co.onmind.hex.infrastructure.webclients.CachedAbcAdapter;
import co.onmind.hex.infrastructure.webclients.dto.AbcToken;
import co.onmind.hex.transverse.RestClientGeneric;
import tools.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

@Configuration
public class RestClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfiguration.class);

    @Value("${app.webclient.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${app.webclient.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
            .requestFactory(createRequestFactory())
            .requestInterceptor(logExchange())
            .build();
    }

    @Bean
    public RestClientGeneric restClientGeneric(RestClient restClient) {
        return new RestClientGeneric(restClient);
    }

    private SimpleClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return factory;
    }

    private ClientHttpRequestInterceptor logExchange() {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Outgoing request: {} {} - Headers: {}",
                    request.getMethod(),
                    request.getURI(),
                    request.getHeaders()
                );
            } else {
                logger.info("Outgoing request: {} {}",
                    request.getMethod(),
                    request.getURI()
                );
            }
            try {
                ClientHttpResponse response = execution.execute(request, body);
                if (logger.isDebugEnabled()) {
                    logger.debug("Incoming response: {} - Headers: {}",
                        response.getStatusCode(),
                        response.getHeaders()
                    );
                } else {
                    logger.info("Incoming response: {}", response.getStatusCode());
                }
                return response;
            } catch (IOException e) {
                logger.error("HTTP exchange failed: {} {} - Error: {}",
                    request.getMethod(), request.getURI(), e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    public RestClient xdbRestClient(
            RestClient.Builder builder,
            @Value("${app.xdb.base-url:http://localhost:9990}") String baseUrl) {
        return builder
            .baseUrl(baseUrl)
            .requestFactory(createRequestFactory())
            .requestInterceptor(logExchange())
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();
    }

    @Bean
    public AbcWebClient abcWebClientBean(
            @Qualifier("xdbRestClient") RestClient xdbRestClient,
            @Value("${app.xdb.auth-type:none}") String authType,
            @Value("${app.xdb.auth-token:}") String authToken) {
        RestClientGeneric xdbRestClientGeneric = new RestClientGeneric(xdbRestClient);
        AbcToken token = switch (authType.toLowerCase()) {
            case "bearer" -> AbcToken.bearer(authToken);
            case "basic" -> {
                String[] parts = authToken.split(":", 2);
                String user = parts.length > 0 ? parts[0] : "";
                String pass = parts.length > 1 ? parts[1] : "";
                yield AbcToken.basic(user, pass);
            }
            default -> AbcToken.none();
        };
        return new AbcWebClient(xdbRestClientGeneric, token);
    }

    @Bean
    @Profile("!grpc")
    public AbcAdapter abcAdapter(
            AbcWebClient abcWebClient,
            CircuitBreaker abcCircuitBreaker) {
        return new AbcAdapter(abcWebClient, abcCircuitBreaker);
    }

    @Bean
    @Profile("!grpc")
    @Primary
    public AbcPort abcPort(
            AbcAdapter abcAdapter,
            CachePort cachePort,
            ObjectMapper objectMapper,
            @Value("${app.xdb.cache.ttl-seconds:300}") int ttlSeconds) {
        return new CachedAbcAdapter(abcAdapter, cachePort, objectMapper,
                Duration.ofSeconds(ttlSeconds));
    }

    @Bean
    public S3Client s3Client(
            @Value("${app.s3.region:us-east-1}") String region,
            @Value("${app.s3.endpoint:#{null}}") String endpoint,
            @Value("${app.s3.force-path-style:true}") boolean forcePathStyle) {

        var builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        if (forcePathStyle) {
            builder.serviceConfiguration(config -> config.pathStyleAccessEnabled(true));
        }

        return builder.build();
    }

    @Bean
    public LambdaClient lambdaClient(
            @Value("${app.lambda.region:us-east-1}") String region,
            @Value("${app.lambda.endpoint:#{null}}") String endpoint) {

        var builder = LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    private static CircuitBreakerConfig createCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
    }

    @Bean
    public CircuitBreaker abcCircuitBreaker() {
        return CircuitBreaker.of("abc", createCircuitBreakerConfig());
    }

    @Bean
    public CircuitBreaker lambdaCircuitBreaker() {
        return CircuitBreaker.of("lambda", createCircuitBreakerConfig());
    }

    @Bean
    public CircuitBreaker s3CircuitBreaker() {
        return CircuitBreaker.of("s3", createCircuitBreakerConfig());
    }

    public static class ExternalServiceException extends RuntimeException {
        private final int statusCode;

        public ExternalServiceException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
