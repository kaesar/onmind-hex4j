package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.ports.in.AbcSheetTrait;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AbcSheetUseCase implements AbcSheetTrait {

    private final AbcPort abcPort;

    public AbcSheetUseCase(AbcPort abcPort) {
        this.abcPort = abcPort;
    }

    @Override
    public SheetResponseDto sheet(String show, String from, String some) {
        return toDto(abcPort.sheet(show, from, some));
    }

    @Override
    public List<SheetResponseDto> sheets(Iterable<SheetRequest> requests) {
        List<SheetResponseDto> results = new ArrayList<>();
        for (SheetRequest request : requests) {
            results.add(sheet(request.show(), request.from(), request.some()));
        }
        return results;
    }

    private SheetResponseDto toDto(AbcResponse response) {
        return new SheetResponseDto(
            response.ok(),
            response.status(),
            response.message(),
            response.total(),
            response.data());
    }
}
