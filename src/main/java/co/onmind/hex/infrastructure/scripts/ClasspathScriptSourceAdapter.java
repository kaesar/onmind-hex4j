package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.ScriptSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ClasspathScriptSourceAdapter implements ScriptSourcePort {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathScriptSourceAdapter.class);

    private final ResourceLoader resourceLoader;
    private final String scriptsLocation;

    public ClasspathScriptSourceAdapter(
            ResourceLoader resourceLoader,
            @Value("${app.scripts.location:classpath:scripts/}") String scriptsLocation) {
        this.resourceLoader = resourceLoader;
        this.scriptsLocation = scriptsLocation.endsWith("/") ? scriptsLocation : scriptsLocation + "/";
    }

    @Override
    public String loadScript(String fileName) {
        try {
            return readFile(fileName);
        } catch (IOException e) {
            logger.error("Failed to load script '{}': {}", fileName, e.getMessage());
            throw new IllegalArgumentException("Script file not readable: " + fileName, e);
        }
    }

    private String readFile(String fileName) throws IOException {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid script file name: " + fileName);
        }

        Resource resource = resourceLoader.getResource(scriptsLocation + fileName);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("Script file not found: " + fileName);
        }

        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
