package co.onmind.hex.domain.models;

import co.onmind.hex.domain.exceptions.ScriptNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ScriptWhitelist {

    private static final Logger logger = LoggerFactory.getLogger(ScriptWhitelist.class);

    private final Set<String> allowedScripts;

    public ScriptWhitelist(@Value("${app.scripts.whitelist:hello.js,example.js,services.js}") String whitelistCsv) {
        this.allowedScripts = Arrays.stream(whitelistCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
        logger.info("Script whitelist initialized with {} entries: {}", allowedScripts.size(), allowedScripts);
    }

    public void requireAllowed(String fileName) {
        if (!isAllowed(fileName)) {
            throw new ScriptNotAllowedException(fileName, allowedFileNames());
        }
    }

    public boolean isAllowed(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        return allowedScripts.contains(fileName.trim().toLowerCase(Locale.ROOT));
    }

    public String allowedFileNames() {
        return String.join(", ", allowedScripts);
    }
}
