package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.out.StoreItemResponseDto;

import java.util.List;

public interface ListStoreTrait {

    List<StoreItemResponseDto> listItems(String bucket);
}
