package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.ScriptingPort;
import co.onmind.hex.application.ports.out.ScriptServicesPort;
import co.onmind.hex.domain.models.ScriptResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.stefanrichterhuber.quickjs.QuickJSContext;
import io.github.stefanrichterhuber.quickjs.QuickJSRuntime;
import io.github.stefanrichterhuber.quickjs.VariadicFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class QuickJsAdapter implements ScriptingPort {

    private static final Logger logger = LoggerFactory.getLogger(QuickJsAdapter.class);
    private static final long SCRIPT_TIMEOUT_SECONDS = 5;
    private static final long SCRIPT_MEMORY_LIMIT_BYTES = 32L * 1024 * 1024;

    private final ScriptServicesPort scriptServices;
    private final ObjectMapper objectMapper;

    public QuickJsAdapter(ScriptServicesPort scriptServices, ObjectMapper objectMapper) {
        this.scriptServices = scriptServices;
        this.objectMapper = objectMapper;
        logger.info("QuickJS engine adapter initialized for sandboxed execution");
    }

    @Override
    public ScriptResult executeScript(String script) {
        StringBuilder stdout = new StringBuilder();
        try (QuickJSRuntime runtime = new QuickJSRuntime();
             QuickJSContext context = runtime.createContext()) {

            runtime.withScriptRuntimeLimit(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            runtime.withMemoryLimit(SCRIPT_MEMORY_LIMIT_BYTES);

            context.setGlobal("services", buildServicesBinding());
            context.setGlobal("console", buildConsoleBinding(stdout));

            Object result = context.eval(script);
            String value = result != null ? result.toString() : null;
            return new ScriptResult(value, stdout.toString(), null);
        } catch (Exception | LinkageError e) {
            // LinkageError (e.g. UnsatisfiedLinkError) covers platforms without
            // QuickJS native libraries — degrade to an error result, never throw.
            String detail = e.getMessage() != null ? e.getMessage() : e.toString();
            logger.warn("Script execution error: {}", detail);
            return new ScriptResult(null, stdout.toString(), detail);
        }
    }

    private Map<String, Object> buildConsoleBinding(StringBuilder stdout) {
        VariadicFunction<Object> log = args -> {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) line.append(' ');
                line.append(args[i] != null ? args[i].toString() : "null");
            }
            logger.debug("script console.log: {}", line);
            synchronized (stdout) {
                if (!stdout.isEmpty()) stdout.append('\n');
                stdout.append(line);
            }
            return null;
        };
        return Map.of("log", log, "info", log, "debug", log, "warn", log, "error", log);
    }

    private Map<String, Object> buildServicesBinding() {
        Map<String, Object> services = new HashMap<>();

        services.put("abcSheet", (VariadicFunction<Object>) args ->
            toJsMap(scriptServices.abcSheet(str(args, 0), str(args, 1), str(args, 2))));

        services.put("abcExec", (VariadicFunction<Object>) args ->
            toJsMap(scriptServices.abcExec(str(args, 0), str(args, 1), str(args, 2), str(args, 3), str(args, 4))));

        services.put("publish", (VariadicFunction<Object>) args -> {
            scriptServices.publish(str(args, 0), str(args, 1), str(args, 2));
            return null;
        });

        services.put("invoke", (VariadicFunction<Object>) args ->
            scriptServices.invoke(str(args, 0), str(args, 1)));

        services.put("invokeAsync", (VariadicFunction<Object>) args -> {
            scriptServices.invokeAsync(str(args, 0), str(args, 1));
            return null;
        });

        services.put("listItems", (VariadicFunction<Object>) args ->
            toJsList(scriptServices.listItems(str(args, 0))));

        services.put("sendEmail", (VariadicFunction<Object>) args -> {
            scriptServices.sendEmail(str(args, 0), str(args, 1), str(args, 2));
            return null;
        });

        services.put("cacheGet", (VariadicFunction<Object>) args ->
            scriptServices.cacheGet(str(args, 0)));

        services.put("cacheSet", (VariadicFunction<Object>) args -> {
            scriptServices.cacheSet(str(args, 0), str(args, 1));
            return null;
        });

        services.put("cacheEvict", (VariadicFunction<Object>) args -> {
            scriptServices.cacheEvict(str(args, 0));
            return null;
        });

        return services;
    }

    private String str(Object[] args, int index) {
        if (args == null || index >= args.length || args[index] == null) {
            return null;
        }
        return args[index].toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toJsMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.convertValue(value, Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toJsList(Object value) {
        if (value == null) {
            return List.of();
        }
        return objectMapper.convertValue(value, List.class);
    }
}
