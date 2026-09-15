package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.out.StoreItemResponseDto;
import co.onmind.hex.application.ports.in.ListStoreTrait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    private final ListStoreTrait listStoreTrait;

    public StoreController(ListStoreTrait listStoreTrait) {
        this.listStoreTrait = listStoreTrait;
    }

    @GetMapping("/items")
    public ResponseEntity<List<StoreItemResponseDto>> listItems(@RequestParam String bucket) {
        logger.info("Listing store items for bucket: {}", bucket);
        List<StoreItemResponseDto> response = listStoreTrait.listItems(bucket);
        logger.info("Found {} items in bucket: {}", response.size(), bucket);
        return ResponseEntity.ok(response);
    }
}
