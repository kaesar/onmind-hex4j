package co.onmind.hex.infrastructure.configuration;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.infrastructure.webclients.AbcAdapter;
import co.onmind.hex.infrastructure.webclients.AbcWebClient;
import co.onmind.hex.infrastructure.webclients.CachedAbcAdapter;
import co.onmind.hex.infrastructure.webclients.dto.AbcToken;
import co.onmind.hex.transverse.WebClientGeneric;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfiguration.class);

    @Value("${app.webclient.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${app.webclient.read-timeout:10000}")
    private int readTimeout;

    @Value("${app.webclient.write-timeout:10000}")
    private int writeTimeout;

    @Value("${app.webclient.max-memory-size:1048576}")
    private int maxMemorySize;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .clientConnector(createClientConnector())
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxMemorySize))
            .filter(logRequest())
            .filter(logResponse())
            .filter(handleErrors())
            .build();
    }

    @Bean
    public WebClientGeneric webClientGeneric(WebClient webClient) {
        return new WebClientGeneric(webClient);
    }

    private ReactorClientHttpConnector createClientConnector() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
            );

        return new ReactorClientHttpConnector(httpClient);
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Outgoing request: {} {} - Headers: {}",
                    clientRequest.method(),
                    clientRequest.url(),
                    clientRequest.headers()
                );
            } else {
                logger.info("Outgoing request: {} {}",
                    clientRequest.method(),
                    clientRequest.url()
                );
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Incoming response: {} - Headers: {}",
                    clientResponse.statusCode(),
                    clientResponse.headers().asHttpHeaders()
                );
            } else {
                logger.info("Incoming response: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("Unknown error")
                    .flatMap(errorBody -> {
                        String errorMessage = String.format(
                            "HTTP %d error: %s",
                            clientResponse.statusCode().value(),
                            errorBody
                        );

                        logger.error("External service error: {}", errorMessage);

                        return Mono.error(new ExternalServiceException(
                            errorMessage,
                            clientResponse.statusCode().value()
                        ));
                    });
            }
            return Mono.just(clientResponse);
        });
    }

    @Bean
    public WebClient xdbWebClient(@Value("${app.xdb.base-url:http://localhost:9990}") String baseUrl) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(createClientConnector())
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxMemorySize))
            .filter(logRequest())
            .filter(logResponse())
            .filter(handleErrors())
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();
    }

    @Bean
    public AbcWebClient abcWebClientBean(
            @Qualifier("xdbWebClient") WebClient xdbWebClient,
            @Value("${app.xdb.auth-type:none}") String authType,
            @Value("${app.xdb.auth-token:}") String authToken) {
        WebClientGeneric xdbWebClientGeneric = new WebClientGeneric(xdbWebClient);
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
        return new AbcWebClient(xdbWebClientGeneric, token);
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
