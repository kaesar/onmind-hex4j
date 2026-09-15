package co.onmind.hex.infrastructure.configuration;

import co.onmind.grpc.proto.AbcServiceGrpc;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.CachePort;
import co.onmind.hex.infrastructure.webclients.CachedAbcAdapter;
import co.onmind.hex.infrastructure.webclients.GrpcAbcAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@Profile("grpc")
public class GrpcConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GrpcConfiguration.class);

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel grpcManagedChannel(
            @Value("${app.xdb.grpc.host:localhost}") String host,
            @Value("${app.xdb.grpc.port:9991}") int port) {
        logger.info("gRPC channel -> {}:{}", host, port);
        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
    }

    @Bean
    public AbcServiceGrpc.AbcServiceBlockingStub grpcAbcServiceStub(ManagedChannel grpcManagedChannel) {
        return AbcServiceGrpc.newBlockingStub(grpcManagedChannel);
    }

    @Bean
    @Primary
    public AbcPort grpcAbcPort(
            AbcServiceGrpc.AbcServiceBlockingStub grpcStub,
            CircuitBreaker abcCircuitBreaker,
            CachePort cachePort,
            ObjectMapper objectMapper,
            @Value("${app.xdb.cache.ttl-seconds:300}") int ttlSeconds) {
        GrpcAbcAdapter grpcAdapter = new GrpcAbcAdapter(grpcStub, abcCircuitBreaker, objectMapper);
        return new CachedAbcAdapter(grpcAdapter, cachePort, objectMapper,
                Duration.ofSeconds(ttlSeconds));
    }
}
