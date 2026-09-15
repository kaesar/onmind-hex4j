package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.in.ExecuteScriptRequestDto;
import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.application.ports.in.ExecuteScriptTrait;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/script")
public class ScriptingController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptingController.class);

    private final ExecuteScriptTrait executeScriptTrait;

    public ScriptingController(ExecuteScriptTrait executeScriptTrait) {
        this.executeScriptTrait = executeScriptTrait;
    }

    @PostMapping("/execute")
    public ResponseEntity<ScriptResultResponseDto> executeScript(
            @Valid @RequestBody ExecuteScriptRequestDto request) {
        logger.info("Executing script: {}", request.script());
        ScriptResultResponseDto response = executeScriptTrait.executeScript(request.script());
        return ResponseEntity.ok(response);
    }
}
