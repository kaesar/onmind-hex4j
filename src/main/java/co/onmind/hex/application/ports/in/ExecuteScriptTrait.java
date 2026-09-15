package co.onmind.hex.application.ports.in;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;

public interface ExecuteScriptTrait {

    ScriptResultResponseDto executeScript(String scriptFileName);
}
