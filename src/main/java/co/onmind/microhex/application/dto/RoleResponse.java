package co.onmind.microhex.application.dto;

import java.time.LocalDateTime;

/**
 * DTO for role responses.
 */
public class RoleResponse {
    
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    
    public RoleResponse() {}
    
    public RoleResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}