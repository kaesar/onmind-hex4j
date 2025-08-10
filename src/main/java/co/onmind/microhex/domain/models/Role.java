package co.onmind.microhex.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Role domain model representing a user role in the system.
 * 
 * This is the core domain entity that encapsulates the business logic
 * and rules for roles. It follows the hexagonal architecture principles
 * by being independent of external frameworks and infrastructure.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
public class Role {
    
    private static final Set<String> RESERVED_NAMES = Set.of("ADMIN", "ROOT", "SYSTEM");
    
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    
    public Role() {}
    
    public Role(String name) {
        validateName(name);
        this.name = name.trim().toUpperCase();
        this.createdAt = LocalDateTime.now();
    }
    
    public Role(Long id, String name, LocalDateTime createdAt) {
        validateName(name);
        this.id = id;
        this.name = name.trim().toUpperCase();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    /**
     * Factory method to create a new role with normalized name.
     * @param name the role name
     * @return a new Role instance
     */
    public static Role create(String name) {
        return new Role(name);
    }
    
    /**
     * Validates if this role is a system role that cannot be deleted.
     * @return true if it's a system role
     */
    public boolean isSystemRole() {
        return name != null && (name.startsWith("SYSTEM_") || RESERVED_NAMES.contains(name));
    }
    
    /**
     * Creates a copy of this role with updated name.
     * @param newName the new name
     * @return a new Role instance with the updated name
     */
    public Role withName(String newName) {
        return new Role(this.id, newName, this.createdAt);
    }
    
    /**
     * Validates the role name according to business rules.
     * @param name the name to validate
     * @throws IllegalArgumentException if the name is invalid
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Role name cannot exceed 100 characters");
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && 
               Objects.equals(name, role.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}