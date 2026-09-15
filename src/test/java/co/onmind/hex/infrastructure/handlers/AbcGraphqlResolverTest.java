package co.onmind.hex.infrastructure.handlers;

import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.ports.in.AbcSheetTrait;
import co.onmind.hex.application.ports.in.AbcSheetTrait.SheetRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbcGraphqlResolverTest {

    @Mock private AbcSheetTrait abcSheetTrait;

    @InjectMocks private AbcGraphqlResolver resolver;

    @Test
    @DisplayName("abcSheet delegates to trait")
    void abcSheet() {
        SheetResponseDto dto = new SheetResponseDto(true, 200, "OK", 1, List.of());
        when(abcSheetTrait.sheet(anyString(), anyString(), anyString())).thenReturn(dto);

        SheetResponseDto result = resolver.abcSheet("s", "f", "c");

        assertTrue(result.ok());
    }

    @Test
    @DisplayName("abcSheets maps inputs and delegates")
    void abcSheets() {
        SheetResponseDto dto = new SheetResponseDto(true, 200, "OK", 1, List.of());
        when(abcSheetTrait.sheets(any())).thenReturn(List.of(dto, dto));

        List<SheetResponseDto> result = resolver.abcSheets(
            List.of(new AbcGraphqlResolver.AbcSheetInput("s", "f", "c")));

        assertEquals(2, result.size());
    }
}
