package co.onmind.hex.transverse.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String REQUEST_ID_KEY = "requestId";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = generateRequestId();
        long startTime = System.currentTimeMillis();

        logIncomingRequest(request, requestId);
        request.setAttribute(REQUEST_ID_KEY, requestId);

        try {
            filterChain.doFilter(request, response);
            logOutgoingResponse(request, response, requestId, startTime);
        } catch (Exception e) {
            logErrorResponse(request, requestId, startTime, e);
            throw e;
        } finally {
            clearMDC();
        }
    }

    private void logIncomingRequest(HttpServletRequest request, String requestId) {
        try {
            MDC.put(REQUEST_ID_KEY, requestId);
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            logger.info("Incoming request: {} {} - RequestId: {} - RemoteAddress: {} - UserAgent: {}",
                request.getMethod(),
                request.getRequestURI(),
                requestId,
                getRemoteAddress(request),
                getUserAgent(request)
            );

            if (request.getQueryString() != null) {
                logger.debug("Query string: {}", request.getQueryString());
            }

            if (logger.isDebugEnabled()) {
                java.util.Collections.list(request.getHeaderNames()).forEach(name ->
                    logger.debug("Header: {} = {}", name, request.getHeader(name)));
            }
        } catch (Exception e) {
            logger.warn("Error logging incoming request", e);
        }
    }

    private void logOutgoingResponse(HttpServletRequest request, HttpServletResponse response,
                                     String requestId, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;

            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("duration", String.valueOf(duration));

            logger.info("Outgoing response: {} {} - Status: {} - Duration: {}ms - RequestId: {}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                requestId
            );

            if (duration > 1000) {
                logger.warn("Slow request detected: {} {} took {}ms - RequestId: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    requestId
                );
            }
        } catch (Exception e) {
            logger.warn("Error logging outgoing response", e);
        }
    }

    private void logErrorResponse(HttpServletRequest request,
                                  String requestId, long startTime, Throwable throwable) {
        try {
            long duration = System.currentTimeMillis() - startTime;

            MDC.put("error", throwable.getClass().getSimpleName());
            MDC.put("duration", String.valueOf(duration));

            logger.error("Error response: {} {} - Error: {} - Duration: {}ms - RequestId: {} - Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                throwable.getClass().getSimpleName(),
                duration,
                requestId,
                throwable.getMessage()
            );
        } catch (Exception e) {
            logger.warn("Error logging error response", e);
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String getRemoteAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }

    private void clearMDC() {
        MDC.clear();
    }
}
