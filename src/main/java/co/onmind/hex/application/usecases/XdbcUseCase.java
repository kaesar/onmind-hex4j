package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.ports.in.XdbcSheetTrait;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import org.springframework.stereotype.Component;

@Component
public class XdbcUseCase implements XdbcSheetTrait {

    private final AbcPort abcPort;

    public XdbcUseCase(AbcPort abcPort) {
        this.abcPort = abcPort;
    }

    @Override
    public SheetResponseDto getSheet() {
        return toDto(abcPort.sheet(null, null, null));
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
