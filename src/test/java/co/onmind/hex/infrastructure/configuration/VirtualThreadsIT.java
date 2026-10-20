package co.onmind.hex.infrastructure.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.filter.OncePerRequestFilter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the virtual-thread setup is really in place at runtime, and that the
 * supporting configuration the virtual threads depend on is correct.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VirtualThreadsIT {

    private static final AtomicBoolean REQUEST_THREAD_WAS_VIRTUAL = new AtomicBoolean(false);

    @TestConfiguration
    static class VirtualThreadProbe {

        @org.springframework.context.annotation.Bean
        FilterRegistrationBean<OncePerRequestFilter> virtualThreadProbe() {
            OncePerRequestFilter filter = new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain filterChain) throws jakarta.servlet.ServletException, java.io.IOException {
                    REQUEST_THREAD_WAS_VIRTUAL.set(Thread.currentThread().isVirtual());
                    filterChain.doFilter(request, response);
                }
            };
            FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
            registration.addUrlPatterns("/*");
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return registration;
        }
    }

    @Autowired
    private ApplicationContext context;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("serves HTTP requests on virtual threads")
    void servesRequestsOnVirtualThreads() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/health")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(REQUEST_THREAD_WAS_VIRTUAL.get(),
                "the request must be handled by a virtual thread — "
                        + "check spring.threads.virtual.enabled=true");
    }

    @Test
    @DisplayName("does not share a single native Redis connection between operations")
    void redisDoesNotShareNativeConnection() {
        LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);

        assertFalse(factory.getShareNativeConnection(),
                "Lettuce must not share one native connection, otherwise concurrent "
                        + "virtual threads serialize on a single socket");
    }

    @Test
    @DisplayName("backs RestClient with the NIO-based JDK HTTP client")
    void restClientUsesJdkHttpClient() {
        ClientHttpRequestFactory factory = context.getBean(ClientHttpRequestFactory.class);

        assertInstanceOf(JdkClientHttpRequestFactory.class, factory,
                "HttpURLConnection (SimpleClientHttpRequestFactory) multiplexes nothing "
                        + "and is the worst fit for virtual threads");
    }
}
