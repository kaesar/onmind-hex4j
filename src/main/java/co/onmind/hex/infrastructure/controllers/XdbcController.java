package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.ports.in.XdbcSheetTrait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/xdb")
public class XdbcController {

    private static final Logger logger = LoggerFactory.getLogger(XdbcController.class);

    private final XdbcSheetTrait xdbcSheetTrait;

    public XdbcController(XdbcSheetTrait xdbcSheetTrait) {
        this.xdbcSheetTrait = xdbcSheetTrait;
    }

    @GetMapping("/sheet")
    public ResponseEntity<SheetResponseDto> getSheet() {
        logger.info("Retrieving XDB sheet");
        SheetResponseDto response = xdbcSheetTrait.getSheet();
        return ResponseEntity.ok(response);
    }
}
