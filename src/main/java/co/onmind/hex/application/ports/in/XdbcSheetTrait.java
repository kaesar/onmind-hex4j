package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.out.SheetResponseDto;

public interface XdbcSheetTrait {

    SheetResponseDto getSheet();
}
