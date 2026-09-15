package co.onmind.hex.infrastructure.scripts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.jupiter.api.Assertions.*;

class ClasspathScriptSourceAdapterTest {

    private final ClasspathScriptSourceAdapter adapter =
        new ClasspathScriptSourceAdapter(new DefaultResourceLoader(), "classpath:scripts/");

    @Test
    @DisplayName("loads hello.js from classpath")
    void loadsHelloScript() {
        String source = adapter.loadScript("hello.js");
        assertNotNull(source);
        assertTrue(source.contains("Hello from hex4j scripts!"));
    }

    @Test
    @DisplayName("rejects path traversal")
    void rejectsTraversal() {
        assertThrows(IllegalArgumentException.class, () -> adapter.loadScript("../evil.js"));
        assertThrows(IllegalArgumentException.class, () -> adapter.loadScript("sub/evil.js"));
    }

    @Test
    @DisplayName("rejects missing files")
    void rejectsMissing() {
        assertThrows(IllegalArgumentException.class, () -> adapter.loadScript("nope.js"));
    }
}
