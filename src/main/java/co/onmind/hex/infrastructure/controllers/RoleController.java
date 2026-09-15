package co.onmind.hex.infrastructure.controllers;

import co.onmind.hex.application.dto.in.CreateRoleRequestDto;
import co.onmind.hex.application.dto.in.UpdateRoleRequestDto;
import co.onmind.hex.application.dto.out.RoleResponseDto;
import co.onmind.hex.application.ports.in.CreateRoleTrait;
import co.onmind.hex.application.ports.in.GetRoleTrait;
import co.onmind.hex.application.usecases.RoleUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final CreateRoleTrait createRoleTrait;
    private final GetRoleTrait getRoleTrait;
    private final RoleUseCase roleUseCase;

    public RoleController(CreateRoleTrait createRoleTrait, GetRoleTrait getRoleTrait, RoleUseCase roleUseCase) {
        this.createRoleTrait = createRoleTrait;
        this.getRoleTrait = getRoleTrait;
        this.roleUseCase = roleUseCase;
    }

    @PostMapping
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody CreateRoleRequestDto request) {
        logger.info("Creating new role with name: {}", request.name());
        RoleResponseDto response = createRoleTrait.createRole(request);
        logger.info("Successfully created role with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateRoleRequestDto request) {
        logger.info("Updating role with ID: {} and new name: {}", id, request.name());
        RoleResponseDto response = roleUseCase.updateRole(id, request.name());
        logger.info("Successfully updated role with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        logger.info("Deleting role with ID: {}", id);
        roleUseCase.deleteRole(id);
        logger.info("Successfully deleted role with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        logger.info("Retrieving role with ID: {}", id);
        RoleResponseDto response = getRoleTrait.getRoleById(id);
        logger.info("Successfully retrieved role: {}", response.name());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        logger.info("Retrieving all roles");
        List<RoleResponseDto> response = getRoleTrait.getAllRoles();
        logger.info("Successfully retrieved {} roles", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoleResponseDto>> searchRoles(@RequestParam String name) {
        logger.info("Searching roles with pattern: {}", name);
        List<RoleResponseDto> response = getRoleTrait.getRolesByNamePattern(name);
        logger.info("Found {} roles matching pattern: {}", response.size(), name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getRoleCount() {
        logger.info("Getting role count");
        long count = roleUseCase.countRoles();
        logger.info("Total roles count: {}", count);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
