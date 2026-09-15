package co.onmind.hex.infrastructure.storage;

import co.onmind.hex.domain.models.StoreItem;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StoreAdapterTest {

    @Mock private S3Client s3Client;

    @Test
    @DisplayName("listItems maps S3 objects to StoreItems")
    void listItems() {
        S3Object object = S3Object.builder()
            .key("docs/report.pdf")
            .size(1024L)
            .lastModified(Instant.parse("2024-01-15T10:30:00Z"))
            .eTag("\"abc123\"")
            .build();
        ListObjectsV2Iterable paginator = mock(ListObjectsV2Iterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<S3Object> contents =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(contents.stream()).thenReturn(List.of(object).stream());
        when(paginator.contents()).thenReturn(contents);
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class))).thenReturn(paginator);

        S3StoreAdapter adapter = new S3StoreAdapter(s3Client, CircuitBreaker.ofDefaults("test"));
        List<StoreItem> items = adapter.listItems("my-bucket");

        assertEquals(1, items.size());
        assertEquals("docs/report.pdf", items.get(0).key());
        assertEquals(1024L, items.get(0).size());
        assertNotNull(items.get(0).lastModified());
        assertEquals("\"abc123\"", items.get(0).eTag());
    }

    @Test
    @DisplayName("listItems returns empty list for empty bucket")
    void listItemsEmpty() {
        ListObjectsV2Iterable paginator = mock(ListObjectsV2Iterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<S3Object> contents =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(contents.stream()).thenReturn(List.<S3Object>of().stream());
        when(paginator.contents()).thenReturn(contents);
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class))).thenReturn(paginator);

        S3StoreAdapter adapter = new S3StoreAdapter(s3Client, CircuitBreaker.ofDefaults("test"));

        assertTrue(adapter.listItems("empty-bucket").isEmpty());
    }
}
