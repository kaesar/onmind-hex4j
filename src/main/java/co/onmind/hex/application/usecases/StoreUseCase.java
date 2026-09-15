package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.out.StoreItemResponseDto;
import co.onmind.hex.application.mappers.StoreMapper;
import co.onmind.hex.application.ports.in.ListStoreTrait;
import co.onmind.hex.application.ports.out.StorePort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StoreUseCase implements ListStoreTrait {

    private final StorePort storePort;
    private final StoreMapper storeMapper;

    public StoreUseCase(StorePort storePort, StoreMapper storeMapper) {
        this.storePort = storePort;
        this.storeMapper = storeMapper;
    }

    @Override
    public List<StoreItemResponseDto> listItems(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        return storeMapper.toResponseDtoList(storePort.listItems(bucket.trim()));
    }
}
