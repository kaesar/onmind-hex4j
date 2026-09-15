package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.ScriptServicesPort;
import co.onmind.hex.domain.models.ScriptResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuickJsAdapterTest {

    private QuickJsAdapter adapter;

    @BeforeEach
    void setUp() {
        ScriptServicesPort services = mock(ScriptServicesPort.class);
        adapter = new QuickJsAdapter(services, new ObjectMapper());
    }

    @Test
    @DisplayName("never throws, even for broken scripts")
    void neverThrows() {
        ScriptResult result = adapter.executeScript("1 + 2");
        assertNotNull(result);
    }

    @Test
    @DisplayName("executes a simple expression and returns its value")
    void executesExpression() {
        ScriptResult result = adapter.executeScript("1 + 2");
        assertEquals("3", result.value());
        assertNull(result.stderr());
    }

    @Test
    @DisplayName("captures console.log into stdout")
    void capturesConsoleLog() {
        ScriptResult result = adapter.executeScript("console.log('hi'); 42;");
        assertEquals("42", result.value());
        assertTrue(result.stdout().contains("hi"));
    }

    @Test
    @DisplayName("captures script errors in stderr instead of failing")
    void capturesErrorsInStderr() {
        ScriptResult result = adapter.executeScript("throw new Error('boom')");
        assertNull(result.value());
        assertNotNull(result.stderr());
        assertTrue(result.stderr().contains("boom"));
    }

    @Test
    @DisplayName("returns object JSON via JSON.stringify")
    void returnsObjectJson() {
        ScriptResult result = adapter.executeScript("JSON.stringify({a: 1})");
        assertEquals("{\"a\":1}", result.value());
    }

    @Test
    @DisplayName("exposes services facade to scripts")
    void exposesServices() {
        ScriptServicesPort services = mock(ScriptServicesPort.class);
        when(services.cacheGet("k")).thenReturn("v");
        QuickJsAdapter adapterWithServices = new QuickJsAdapter(services, new ObjectMapper());

        ScriptResult result = adapterWithServices.executeScript("services.cacheGet('k')");

        assertEquals("v", result.value());
    }
}
