package co.onmind.hex.infrastructure.storage;

import co.onmind.hex.application.ports.out.StorePort;
import co.onmind.hex.domain.models.StoreItem;
import co.onmind.hex.transverse.resilience.OutboundGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class S3StoreAdapter implements StorePort {

    private static final Logger logger = LoggerFactory.getLogger(S3StoreAdapter.class);

    private final S3Client s3Client;
    private final OutboundGuard guard;

    public S3StoreAdapter(S3Client s3Client, OutboundGuard s3Guard) {
        this.s3Client = s3Client;
        this.guard = s3Guard;
    }

    @Override
    public List<StoreItem> listItems(String bucket) {
        logger.debug("S3 listItems bucket={}", bucket);
        return guard.execute(() -> {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();
            List<StoreItem> items = s3Client.listObjectsV2Paginator(request)
                .contents()
                .stream()
                .map(this::toStoreItem)
                .toList();
            logger.debug("S3 listItems bucket={} count={}", bucket, items.size());
            return items;
        });
    }

    private StoreItem toStoreItem(S3Object s3Object) {
        LocalDateTime lastModified = s3Object.lastModified() != null
            ? LocalDateTime.ofInstant(s3Object.lastModified(), ZoneOffset.UTC)
            : null;
        return new StoreItem(
            s3Object.key(),
            s3Object.size(),
            lastModified,
            s3Object.eTag());
    }
}
