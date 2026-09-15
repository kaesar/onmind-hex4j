package co.onmind.hex.application.ports.out;

import co.onmind.hex.domain.models.ScriptResult;

public interface ScriptingPort {

    ScriptResult executeScript(String script);
}
