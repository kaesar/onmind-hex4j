package co.onmind.microhex.infrastructure.persistence.adapters;

import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.domain.ports.out.RoleRepositoryPort;
import co.onmind.microhex.infrastructure.persistence.entities.RoleEntity;
import co.onmind.microhex.infrastructure.persistence.mappers.RoleEntityMapper;
import co.onmind.microhex.infrastructure.persistence.repositories.JpaRoleRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the RoleRepositoryPort.
 * 
 * This adapter implements the output port for role persistence using JPA.
 * It acts as a bridge between the hexagonal architecture's port interface
 * and the JPA repository implementation, handling the conversion between
 * domain models and JPA entities.
 * 
 * The class is annotated with @Repository to be managed by Spring and
 * uses @Transactional to ensure proper transaction management.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Repository
@Transactional
public class RoleRepositoryAdapter implements RoleRepositoryPort {
    
    private final JpaRoleRepository jpaRepository;
    private final RoleEntityMapper entityMapper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param jpaRepository The JPA repository for database operations
     * @param entityMapper The mapper for converting between domain and entity objects
     */
    public RoleRepositoryAdapter(JpaRoleRepository jpaRepository, RoleEntityMapper entityMapper) {
        this.jpaRepository = jpaRepository;
        this.entityMapper = entityMapper;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Role save(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        RoleEntity entity;
        if (role.getId() == null) {
            // Creating new role - don't set ID to let database generate it
            entity = entityMapper.toNewEntity(role);
        } else {
            // Updating existing role
            entity = entityMapper.toEntity(role);
        }
        
        RoleEntity savedEntity = jpaRepository.save(entity);
        return entityMapper.toDomain(savedEntity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        return jpaRepository.findById(id)
                .map(entityMapper::toDomain);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        List<RoleEntity> entities = jpaRepository.findAll();
        return entityMapper.toDomainList(entities);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        
        return jpaRepository.existsByName(name.trim());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        
        return jpaRepository.findByName(name.trim())
                .map(entityMapper::toDomain);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> findByNameContaining(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or blank");
        }
        
        List<RoleEntity> entities = jpaRepository.findByNameContainingIgnoreCase(pattern.trim());
        return entityMapper.toDomainList(entities);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return jpaRepository.count();
    }
    
    /**
     * Additional method to check if a role exists by name (case-insensitive).
     * This method extends the port interface with infrastructure-specific functionality.
     * 
     * @param name The name to check (case-insensitive)
     * @return true if a role with the name exists, false otherwise
     */
    public boolean existsByNameIgnoreCase(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        
        return jpaRepository.existsByNameIgnoreCase(name.trim());
    }
    
    /**
     * Additional method to find a role by name (case-insensitive).
     * This method extends the port interface with infrastructure-specific functionality.
     * 
     * @param name The name to search for (case-insensitive)
     * @return Optional containing the role if found, empty otherwise
     */
    public Optional<Role> findByNameIgnoreCase(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        
        return jpaRepository.findByNameIgnoreCase(name.trim())
                .map(entityMapper::toDomain);
    }
}