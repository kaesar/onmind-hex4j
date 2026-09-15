package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.in.SendEmailRequestDto;
import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.dto.out.StoreItemResponseDto;
import co.onmind.hex.application.mappers.StoreMapper;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.EmailPort;
import co.onmind.hex.application.ports.out.StorePort;
import co.onmind.hex.domain.models.StoreItem;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiscUseCasesTest {

    @Mock private StorePort storePort;
    @Mock private EmailPort emailPort;
    @Mock private AbcPort abcPort;

    @Test
    @DisplayName("StoreUseCase lists and maps items")
    void storeUseCase() {
        StoreMapper mapper = Mappers.getMapper(StoreMapper.class);
        when(storePort.listItems("bucket")).thenReturn(List.of(
            new StoreItem("a.txt", 10L, LocalDateTime.now(), "etag")));

        List<StoreItemResponseDto> result = new StoreUseCase(storePort, mapper).listItems("bucket");

        assertEquals(1, result.size());
        assertEquals("a.txt", result.get(0).key());
    }

    @Test
    @DisplayName("StoreUseCase rejects blank bucket")
    void storeUseCaseBlank() {
        StoreMapper mapper = Mappers.getMapper(StoreMapper.class);
        assertThrows(IllegalArgumentException.class,
            () -> new StoreUseCase(storePort, mapper).listItems("  "));
    }

    @Test
    @DisplayName("SendEmailUseCase delegates with normalized cc")
    void sendEmailUseCase() {
        SendEmailUseCase useCase = new SendEmailUseCase(emailPort);
        SendEmailRequestDto request = new SendEmailRequestDto(
            "to@test.com", "subject", null, null, "body");

        useCase.sendEmail(request);

        verify(emailPort).send("to@test.com", "subject", "body", null, List.of());
    }

    @Test
    @DisplayName("AbcSheetUseCase maps sheet and sheets")
    void abcSheetUseCase() {
        AbcSheetUseCase useCase = new AbcSheetUseCase(abcPort);
        AbcResponse response = new AbcResponse(true, 200, "OK", 1, List.of());
        when(abcPort.sheet("s", "f", "c")).thenReturn(response);

        SheetResponseDto dto = useCase.sheet("s", "f", "c");

        assertTrue(dto.ok());
        assertEquals(200, dto.status());
        assertEquals(1, useCase.sheets(List.of(
            new co.onmind.hex.application.ports.in.AbcSheetTrait.SheetRequest("s", "f", "c"))).size());
    }

    @Test
    @DisplayName("XdbcUseCase returns default sheet")
    void xdbcUseCase() {
        XdbcUseCase useCase = new XdbcUseCase(abcPort);
        AbcResponse response = new AbcResponse(true, 200, "OK", 2, List.of());
        when(abcPort.sheet(null, null, null)).thenReturn(response);

        SheetResponseDto dto = useCase.getSheet();

        assertTrue(dto.ok());
        assertEquals(2, dto.total());
    }
}
