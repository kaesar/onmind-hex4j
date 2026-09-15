package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.ScriptServicesPort;
import co.onmind.hex.domain.models.ScriptResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

class QuickJsAdapterTest {

    private QuickJsAdapter adapter;

    @BeforeEach
    void setUp() {
        ScriptServicesPort services = mock(ScriptServicesPort.class);
        adapter = new QuickJsAdapter(services, new ObjectMapper());
    }

    private static boolean nativeAvailable() {
        try (var runtime = new io.github.stefanrichterhuber.quickjs.QuickJSRuntime();
             var context = runtime.createContext()) {
            return "3".equals(String.valueOf(context.eval("1 + 2")));
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    @DisplayName("never throws, even when natives are missing")
    void neverThrows() {
        ScriptResult result = adapter.executeScript("1 + 2");
        assertNotNull(result);
    }

    @Test
    @DisplayName("executes a simple expression and returns its value")
    void executesExpression() {
        assumeTrue(nativeAvailable(), "QuickJS natives unavailable on this platform");
        ScriptResult result = adapter.executeScript("1 + 2");
        assertEquals("3", result.value());
        assertNull(result.stderr());
    }

    @Test
    @DisplayName("captures console.log into stdout")
    void capturesConsoleLog() {
        assumeTrue(nativeAvailable(), "QuickJS natives unavailable on this platform");
        ScriptResult result = adapter.executeScript("console.log('hi'); 42;");
        assertEquals("42", result.value());
        assertTrue(result.stdout().contains("hi"));
    }

    @Test
    @DisplayName("captures script errors in stderr instead of failing")
    void capturesErrorsInStderr() {
        assumeTrue(nativeAvailable(), "QuickJS natives unavailable on this platform");
        ScriptResult result = adapter.executeScript("throw new Error('boom')");
        assertNull(result.value());
        assertNotNull(result.stderr());
        assertTrue(result.stderr().contains("boom"));
    }

    @Test
    @DisplayName("returns object JSON via JSON.stringify")
    void returnsObjectJson() {
        assumeTrue(nativeAvailable(), "QuickJS natives unavailable on this platform");
        ScriptResult result = adapter.executeScript("JSON.stringify({a: 1})");
        assertEquals("{\"a\":1}", result.value());
    }

    @Test
    @DisplayName("exposes services facade to scripts")
    void exposesServices() {
        assumeTrue(nativeAvailable(), "QuickJS natives unavailable on this platform");
        ScriptServicesPort services = mock(ScriptServicesPort.class);
        org.mockito.Mockito.when(services.cacheGet("k")).thenReturn("v");
        QuickJsAdapter adapterWithServices = new QuickJsAdapter(services, new ObjectMapper());

        ScriptResult result = adapterWithServices.executeScript("services.cacheGet('k')");

        assertEquals("v", result.value());
    }
}
