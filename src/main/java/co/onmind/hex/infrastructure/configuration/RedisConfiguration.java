package co.onmind.hex.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis connection tuning required by virtual threads.
 * <p>
 * {@link LettuceConnectionFactory} shares a <em>single</em> native connection by default
 * ({@code shareNativeConnection = true}). Every {@code GET}/{@code SET} executed through
 * {@code RedisTemplate} therefore travels over that one socket, and Spring Data Redis
 * explicitly documents that {@code RedisConnection} instances are not thread-safe for
 * blocking and transactional operations.
 * <p>
 * That default is survivable with a fixed Tomcat pool, but it becomes a serialization
 * bottleneck once requests are no longer capped by the web server: thousands of virtual
 * threads would queue on a single socket. Disabling the sharing — combined with the
 * Lettuce pool enabled in {@code application.yml} — gives every blocking operation its
 * own pooled connection.
 * <p>
 * The factory itself is still created by Spring Boot's auto-configuration, so every
 * other Redis feature (cluster, sentinel, SSL, {@code spring.data.redis.url}) keeps
 * working; only the sharing flag is overridden.
 */
@Configuration
public class RedisConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    @Bean
    public static BeanPostProcessor lettuceConnectionSharingCustomizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof LettuceConnectionFactory factory
                        && factory.getShareNativeConnection()) {
                    factory.setShareNativeConnection(false);
                    logger.info("Lettuce native connection sharing disabled — "
                            + "blocking operations will use the pooled connections");
                }
                return bean;
            }
        };
    }
}
