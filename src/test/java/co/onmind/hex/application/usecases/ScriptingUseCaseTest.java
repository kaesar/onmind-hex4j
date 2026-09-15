package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.mappers.ScriptingMapper;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.ScriptSourcePort;
import co.onmind.hex.application.ports.out.ScriptingPort;
import co.onmind.hex.application.ports.out.StorePort;
import co.onmind.hex.domain.exceptions.ScriptNotAllowedException;
import co.onmind.hex.domain.models.ScriptResult;
import co.onmind.hex.domain.models.ScriptWhitelist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScriptingUseCaseTest {

    @Mock private ScriptSourcePort scriptSourcePort;
    @Mock private ScriptingPort scriptingPort;
    @Mock private StorePort storePort;
    @Mock private AbcPort abcPort;

    private ScriptingMapper scriptingMapper;
    private ScriptWhitelist whitelist;
    private ScriptingUseCase useCase;

    @BeforeEach
    void setUp() {
        scriptingMapper = Mappers.getMapper(ScriptingMapper.class);
        whitelist = new ScriptWhitelist("hello.js,example.js");
        useCase = new ScriptingUseCase(
            scriptSourcePort, scriptingPort, scriptingMapper, storePort, abcPort, whitelist);
    }

    @Test
    @DisplayName("executeScript loads, executes and maps whitelisted script")
    void executeScriptOk() {
        when(scriptSourcePort.loadScript("hello.js")).thenReturn("\"hi\"");
        when(scriptingPort.executeScript("\"hi\""))
            .thenReturn(new ScriptResult("hi", "", null));

        ScriptResultResponseDto result = useCase.executeScript("hello.js");

        assertEquals("hi", result.value());
        assertEquals("", result.stdout());
        assertNull(result.stderr());
    }

    @Test
    @DisplayName("executeScript rejects non-whitelisted script")
    void executeScriptNotAllowed() {
        assertThrows(ScriptNotAllowedException.class,
            () -> useCase.executeScript("evil.js"));
        verifyNoInteractions(scriptSourcePort, scriptingPort);
    }
}
