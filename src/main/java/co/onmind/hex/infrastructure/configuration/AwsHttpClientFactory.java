package co.onmind.hex.infrastructure.configuration;

import software.amazon.awssdk.http.apache.ApacheHttpClient;

/**
 * Factory for the HTTP client used by the synchronous AWS SDK clients.
 * <p>
 * The AWS SDK default is 50 concurrent connections per client. That limit was fine
 * while Tomcat capped the request concurrency at the same order of magnitude, but
 * with virtual threads the web server is no longer the bottleneck — the SDK
 * connection pool becomes it, and requests queue on the Apache client instead of
 * reaching S3/SQS/SNS/Lambda.
 */
final class AwsHttpClientFactory {

    private AwsHttpClientFactory() {
    }

    /**
     * Builds an Apache HTTP client builder with the given maximum connections.
     *
     * @param maxConnections maximum number of concurrent connections
     * @return a builder ready to be attached to an AWS SDK client
     */
    static ApacheHttpClient.Builder httpClientBuilder(int maxConnections) {
        return ApacheHttpClient.builder()
                .maxConnections(Math.max(1, maxConnections));
    }
}
