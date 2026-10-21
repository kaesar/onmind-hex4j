package co.onmind.hex.infrastructure.configuration;

import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.infrastructure.webclients.AbcAdapter;
import co.onmind.hex.infrastructure.webclients.AbcWebClient;
import co.onmind.hex.infrastructure.webclients.CachedAbcAdapter;
import co.onmind.hex.infrastructure.webclients.dto.AbcToken;
import co.onmind.hex.transverse.resilience.OutboundGuard;
import co.onmind.hex.transverse.RestClientGeneric;
import tools.jackson.databind.ObjectMapper;
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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfiguration.class);

    @Value("${app.webclient.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${app.webclient.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public RestClient restClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory) {
        return builder
            .requestFactory(requestFactory)
            .requestInterceptor(logExchange())
            .build();
    }

    @Bean
    public RestClientGeneric restClientGeneric(RestClient restClient) {
        return new RestClientGeneric(restClient);
    }

    /**
     * Creates the request factory backed by the JDK {@link java.net.http.HttpClient}.
     * <p>
     * The JDK client is NIO-based and multiplexes connections over a small number of
     * event loops, which is the profile virtual threads need: thousands of concurrent
     * requests share the same connections instead of opening one socket per request.
     * {@code SimpleClientHttpRequestFactory} (HttpURLConnection) was previously used
     * and is the worst possible choice under concurrency — it keeps a global
     * keep-alive cache per destination, does not support HTTP/2 and has no
     * per-request read timeout.
     * </p>
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(connectTimeout))
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
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
            ClientHttpRequestFactory requestFactory,
            @Value("${app.xdb.base-url:http://localhost:9990}") String baseUrl) {
        return builder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
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
            @Qualifier("abcGuard") OutboundGuard abcGuard) {
        return new AbcAdapter(abcWebClient, abcGuard);
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
            @Value("${app.s3.force-path-style:true}") boolean forcePathStyle,
            @Value("${app.aws.max-connections:100}") int maxConnections) {

        var builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .httpClientBuilder(AwsHttpClientFactory.httpClientBuilder(maxConnections));

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
            @Value("${app.lambda.endpoint:#{null}}") String endpoint,
            @Value("${app.aws.max-connections:100}") int maxConnections) {

        var builder = LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClientBuilder(AwsHttpClientFactory.httpClientBuilder(maxConnections));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
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
