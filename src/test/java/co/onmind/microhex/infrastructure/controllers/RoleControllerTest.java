package co.onmind.microhex.infrastructure.controllers;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;
import co.onmind.microhex.application.ports.in.CreateRoleUseCase;
import co.onmind.microhex.application.ports.in.GetRoleUseCase;
import co.onmind.microhex.domain.exceptions.DuplicateRoleException;
import co.onmind.microhex.domain.exceptions.RoleNotFoundException;
import co.onmind.microhex.transverse.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RoleController.
 * 
 * These tests verify the REST controller behavior including:
 * - Successful operations
 * - Error handling
 * - Input validation
 * - HTTP status codes
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@WebMvcTest({RoleController.class, GlobalExceptionHandler.class})
class RoleControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CreateRoleUseCase createRoleUseCase;
    
    @MockBean
    private GetRoleUseCase getRoleUseCase;
    
    @Test
    void createRole_WithValidRequest_ShouldReturnCreatedRole() throws Exception {
        // Given
        CreateRoleRequestDto request = new CreateRoleRequestDto("ADMIN");
        RoleResponseDto response = new RoleResponseDto(1L, "ADMIN", LocalDateTime.now());
        
        when(createRoleUseCase.createRole(any(CreateRoleRequestDto.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }
    
    @Test
    void createRole_WithBlankName_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateRoleRequestDto request = new CreateRoleRequestDto("");
        
        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void createRole_WithDuplicateName_ShouldReturnConflict() throws Exception {
        // Given
        CreateRoleRequestDto request = new CreateRoleRequestDto("ADMIN");
        
        when(createRoleUseCase.createRole(any(CreateRoleRequestDto.class)))
                .thenThrow(DuplicateRoleException.forName("ADMIN"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Role with name 'ADMIN' already exists"));
    }
    
    @Test
    void getAllRoles_ShouldReturnAllRoles() throws Exception {
        // Given
        List<RoleResponseDto> roles = Arrays.asList(
                new RoleResponseDto(1L, "ADMIN", LocalDateTime.now()),
                new RoleResponseDto(2L, "USER", LocalDateTime.now())
        );
        
        when(getRoleUseCase.getAllRoles()).thenReturn(roles);
        
        // When & Then
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ADMIN"))
                .andExpect(jsonPath("$[1].name").value("USER"));
    }
    
    @Test
    void getRoleById_WithValidId_ShouldReturnRole() throws Exception {
        // Given
        RoleResponseDto role = new RoleResponseDto(1L, "ADMIN", LocalDateTime.now());
        
        when(getRoleUseCase.getRoleById(eq(1L))).thenReturn(role);
        
        // When & Then
        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }
    
    @Test
    void getRoleById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        when(getRoleUseCase.getRoleById(eq(999L)))
                .thenThrow(RoleNotFoundException.forId(999L));
        
        // When & Then
        mockMvc.perform(get("/api/v1/roles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Role with ID 999 not found"));
    }
    
    @Test
    void getRoleById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/roles/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Role ID must be a positive number"));
    }
}