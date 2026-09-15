package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.out.SheetResponseDto;

import java.util.List;

public interface AbcSheetTrait {

    SheetResponseDto sheet(String show, String from, String some);

    List<SheetResponseDto> sheets(Iterable<SheetRequest> requests);

    record SheetRequest(String show, String from, String some) {}
}
