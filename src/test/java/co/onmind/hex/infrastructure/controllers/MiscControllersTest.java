package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.in.ExecuteScriptRequestDto;
import co.onmind.hex.application.dto.in.SendEmailRequestDto;
import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.dto.out.StoreItemResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.in.ListStoreTrait;
import co.onmind.hex.application.ports.in.SendEmailTrait;
import co.onmind.hex.application.ports.in.XdbcSheetTrait;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiscControllersTest {

    @Mock private ExecuteScriptTrait executeScriptTrait;
    @Mock private ListStoreTrait listStoreTrait;
    @Mock private XdbcSheetTrait xdbcSheetTrait;
    @Mock private SendEmailTrait sendEmailTrait;

    @InjectMocks private ScriptingController scriptingController;
    @InjectMocks private StoreController storeController;
    @InjectMocks private XdbcController xdbcController;
    @InjectMocks private EmailController emailController;

    @Test
    @DisplayName("ScriptingController executes script")
    void scriptingController() {
        ScriptResultResponseDto dto = new ScriptResultResponseDto("hi", "", null);
        when(executeScriptTrait.executeScript(anyString())).thenReturn(dto);

        ResponseEntity<ScriptResultResponseDto> result =
            scriptingController.executeScript(new ExecuteScriptRequestDto("hello.js"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("hi", result.getBody().value());
    }

    @Test
    @DisplayName("StoreController lists items")
    void storeController() {
        List<StoreItemResponseDto> items = List.of(
            new StoreItemResponseDto("a.txt", 1L, null, "etag"));
        when(listStoreTrait.listItems("bucket")).thenReturn(items);

        ResponseEntity<List<StoreItemResponseDto>> result = storeController.listItems("bucket");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("XdbcController returns sheet")
    void xdbcController() {
        SheetResponseDto dto = new SheetResponseDto(true, 200, "OK", 1, List.of());
        when(xdbcSheetTrait.getSheet()).thenReturn(dto);

        ResponseEntity<SheetResponseDto> result = xdbcController.getSheet();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().ok());
    }

    @Test
    @DisplayName("EmailController sends email")
    void emailController() {
        SendEmailRequestDto request = new SendEmailRequestDto(
            "to@test.com", "subject", null, null, "body");

        ResponseEntity<Map<String, String>> result = emailController.sendEmail(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Email queued successfully", result.getBody().get("message"));
        verify(sendEmailTrait).sendEmail(any(SendEmailRequestDto.class));
    }
}
