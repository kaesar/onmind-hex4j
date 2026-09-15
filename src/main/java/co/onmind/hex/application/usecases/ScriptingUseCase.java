package co.onmind.hex.application.usecases;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.mappers.ScriptingMapper;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.application.ports.out.ScriptSourcePort;
import co.onmind.hex.application.ports.out.ScriptingPort;
import co.onmind.hex.application.ports.out.StorePort;
import co.onmind.hex.domain.models.ScriptResult;
import co.onmind.hex.domain.models.ScriptWhitelist;
import co.onmind.hex.domain.models.StoreItem;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScriptingUseCase implements ExecuteScriptTrait {

    private final ScriptSourcePort scriptSourcePort;
    private final ScriptingPort scriptingPort;
    private final ScriptingMapper scriptingMapper;
    private final StorePort storePort;
    private final AbcPort abcPort;
    private final ScriptWhitelist scriptWhitelist;

    public ScriptingUseCase(
            ScriptSourcePort scriptSourcePort,
            ScriptingPort scriptingPort,
            ScriptingMapper scriptingMapper,
            StorePort storePort,
            AbcPort abcPort,
            ScriptWhitelist scriptWhitelist) {
        this.scriptSourcePort = scriptSourcePort;
        this.scriptingPort = scriptingPort;
        this.scriptingMapper = scriptingMapper;
        this.storePort = storePort;
        this.abcPort = abcPort;
        this.scriptWhitelist = scriptWhitelist;
    }

    @Override
    public ScriptResultResponseDto executeScript(String scriptFileName) {
        scriptWhitelist.requireAllowed(scriptFileName);
        String source = scriptSourcePort.loadScript(scriptFileName);
        ScriptResult result = scriptingPort.executeScript(source);
        return scriptingMapper.toResponseDto(result);
    }

    public List<StoreItem> listItems(String bucket) {
        return storePort.listItems(bucket);
    }

    public AbcResponse abcSheet(String show, String from, String some) {
        return abcPort.sheet(show, from, some);
    }
}
