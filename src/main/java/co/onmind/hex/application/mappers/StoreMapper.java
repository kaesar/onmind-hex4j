package co.onmind.hex.application.mappers;

import co.onmind.hex.application.dto.out.StoreItemResponseDto;
import co.onmind.hex.domain.models.StoreItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    default StoreItemResponseDto toResponseDto(StoreItem item) {
        if (item == null) {
            return null;
        }
        return new StoreItemResponseDto(item.key(), item.size(), item.lastModified(), item.eTag());
    }

    default List<StoreItemResponseDto> toResponseDtoList(List<StoreItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(this::toResponseDto).toList();
    }
}
