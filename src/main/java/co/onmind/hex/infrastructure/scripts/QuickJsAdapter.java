package co.onmind.hex.infrastructure.scripts;

import co.onmind.hex.application.ports.out.ScriptingPort;
import co.onmind.hex.application.ports.out.ScriptServicesPort;
import co.onmind.hex.domain.models.ScriptResult;
import com.caoccao.qjs4j.core.JSBoolean;
import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSNativeFunction;
import com.caoccao.qjs4j.core.JSNull;
import com.caoccao.qjs4j.core.JSNumber;
import com.caoccao.qjs4j.core.JSObject;
import com.caoccao.qjs4j.core.JSRuntime;
import com.caoccao.qjs4j.core.JSRuntimeOptions;
import com.caoccao.qjs4j.core.JSString;
import com.caoccao.qjs4j.core.JSUndefined;
import com.caoccao.qjs4j.core.JSValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class QuickJsAdapter implements ScriptingPort {

    private static final Logger logger = LoggerFactory.getLogger(QuickJsAdapter.class);
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
        // NOTE (qjs4j 0.1.1): setMaxMemoryUsage bounds ArrayBuffer/SharedArrayBuffer
        // allocations, other heap usage is bounded by -Xmx. There is no script
        // time-limit API in 0.1.1 (interrupt support only exists on unreleased main),
        // so the script whitelist remains the primary sandbox control.
        JSRuntimeOptions options = new JSRuntimeOptions()
            .setMaxMemoryUsage(SCRIPT_MEMORY_LIMIT_BYTES);
        try (JSRuntime runtime = new JSRuntime(options);
             JSContext context = runtime.createContext()) {
            context.getGlobalObject().set("services", buildServicesObject(context));
            context.getGlobalObject().set("console", buildConsoleObject(context, stdout));

            JSValue result = context.eval(script);
            Object javaResult = result != null ? result.toJavaObject() : null;
            String value = formatResult(javaResult);
            return new ScriptResult(value, stdout.toString(), null);
        } catch (Exception e) {
            String detail = e.getMessage() != null ? e.getMessage() : e.toString();
            logger.warn("Script execution error: {}", detail);
            return new ScriptResult(null, stdout.toString(), detail);
        }
    }

    private JSObject buildConsoleObject(JSContext context, StringBuilder stdout) {
        JSNativeFunction log = new JSNativeFunction(context, "log", 1, (ctx, thisArg, args) -> {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) line.append(' ');
                Object arg = args[i] != null ? args[i].toJavaObject() : null;
                line.append(arg != null ? arg.toString() : "null");
            }
            logger.debug("script console.log: {}", line);
            synchronized (stdout) {
                if (!stdout.isEmpty()) stdout.append('\n');
                stdout.append(line);
            }
            return new JSUndefined();
        });
        JSObject console = context.createJSObject();
        console.set("log", log);
        console.set("info", log);
        console.set("debug", log);
        console.set("warn", log);
        console.set("error", log);
        return console;
    }

    private JSObject buildServicesObject(JSContext context) {
        JSObject services = context.createJSObject();

        services.set("abcSheet", new JSNativeFunction(context, "abcSheet", 3, (ctx, thisArg, args) ->
            toJsValue(ctx, toJsMap(scriptServices.abcSheet(str(args, 0), str(args, 1), str(args, 2))))));

        services.set("abcExec", new JSNativeFunction(context, "abcExec", 5, (ctx, thisArg, args) ->
            toJsValue(ctx, toJsMap(scriptServices.abcExec(
                str(args, 0), str(args, 1), str(args, 2), str(args, 3), str(args, 4))))));

        services.set("publish", new JSNativeFunction(context, "publish", 3, (ctx, thisArg, args) -> {
            scriptServices.publish(str(args, 0), str(args, 1), str(args, 2));
            return new JSUndefined();
        }));

        services.set("invoke", new JSNativeFunction(context, "invoke", 2, (ctx, thisArg, args) ->
            toJsValue(ctx, scriptServices.invoke(str(args, 0), str(args, 1)))));

        services.set("invokeAsync", new JSNativeFunction(context, "invokeAsync", 2, (ctx, thisArg, args) -> {
            scriptServices.invokeAsync(str(args, 0), str(args, 1));
            return new JSUndefined();
        }));

        services.set("listItems", new JSNativeFunction(context, "listItems", 1, (ctx, thisArg, args) ->
            toJsValue(ctx, toJsList(scriptServices.listItems(str(args, 0))))));

        services.set("sendEmail", new JSNativeFunction(context, "sendEmail", 3, (ctx, thisArg, args) -> {
            scriptServices.sendEmail(str(args, 0), str(args, 1), str(args, 2));
            return new JSUndefined();
        }));

        services.set("cacheGet", new JSNativeFunction(context, "cacheGet", 1, (ctx, thisArg, args) ->
            toJsValue(ctx, scriptServices.cacheGet(str(args, 0)))));

        services.set("cacheSet", new JSNativeFunction(context, "cacheSet", 2, (ctx, thisArg, args) -> {
            scriptServices.cacheSet(str(args, 0), str(args, 1));
            return new JSUndefined();
        }));

        services.set("cacheEvict", new JSNativeFunction(context, "cacheEvict", 1, (ctx, thisArg, args) -> {
            scriptServices.cacheEvict(str(args, 0));
            return new JSUndefined();
        }));

        return services;
    }

    private String str(JSValue[] args, int index) {
        if (args == null || index >= args.length || args[index] == null) {
            return null;
        }
        Object value = args[index].toJavaObject();
        return formatResult(value);
    }

    private String formatResult(Object value) {
        if (value == null) {
            return null;
        }
        // qjs4j materializes every number as Double — render integral values
        // without the trailing ".0" to keep the ScriptResult contract stable.
        if (value instanceof Double d && d == Math.rint(d) && !Double.isInfinite(d)
                && Math.abs(d) < 9.007199254740992E15) {
            return Long.toString(d.longValue());
        }
        if (value instanceof Float f && f == Math.rint(f) && !Float.isInfinite(f)
                && Math.abs(f) < 9.007199254740992E15f) {
            return Long.toString(f.longValue());
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toJsMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.convertValue(value, Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Object> toJsList(Object value) {
        if (value == null) {
            return List.of();
        }
        return objectMapper.convertValue(value, List.class);
    }

    private JSValue toJsValue(JSContext context, Object value) {
        if (value == null) {
            return new JSNull();
        }
        if (value instanceof String s) {
            return new JSString(s);
        }
        if (value instanceof Integer i) {
            return new JSNumber(i);
        }
        if (value instanceof Long l) {
            return new JSNumber(l);
        }
        if (value instanceof Number n) {
            return new JSNumber(n.doubleValue());
        }
        if (value instanceof Boolean b) {
            return new JSBoolean(b);
        }
        if (value instanceof Map<?, ?> map) {
            JSObject object = context.createJSObject();
            map.forEach((key, entryValue) ->
                object.set(String.valueOf(key), toJsValue(context, entryValue)));
            return object;
        }
        if (value instanceof Iterable<?> iterable) {
            List<JSValue> items = new ArrayList<>();
            iterable.forEach(item -> items.add(toJsValue(context, item)));
            return context.createJSArray(items.toArray(new JSValue[0]));
        }
        return new JSString(value.toString());
    }
}
