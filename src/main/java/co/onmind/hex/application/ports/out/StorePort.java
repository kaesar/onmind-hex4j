package co.onmind.hex.application.ports.out;

import co.onmind.hex.domain.models.StoreItem;

import java.util.List;

public interface StorePort {

    List<StoreItem> listItems(String bucket);
}
