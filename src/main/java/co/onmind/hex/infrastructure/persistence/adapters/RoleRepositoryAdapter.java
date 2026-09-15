package co.onmind.hex.infrastructure.persistence.adapters;

import co.onmind.hex.application.ports.out.RoleRepositoryPort;
import co.onmind.hex.domain.models.Role;
import co.onmind.hex.infrastructure.persistence.entities.RoleEntity;
import co.onmind.hex.infrastructure.persistence.mappers.RoleEntityMapper;
import co.onmind.hex.infrastructure.persistence.repositories.JpaRoleRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final JpaRoleRepository jpaRepository;
    private final RoleEntityMapper entityMapper;

    public RoleRepositoryAdapter(JpaRoleRepository jpaRepository, RoleEntityMapper entityMapper) {
        this.jpaRepository = jpaRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    public Role save(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        RoleEntity entity;
        if (role.getId() == null) {
            entity = entityMapper.toNewEntity(role);
        } else {
            entity = entityMapper.toEntity(role);
        }

        RoleEntity savedEntity = jpaRepository.save(entity);
        return entityMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        return jpaRepository.findById(id)
                .map(entityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        List<RoleEntity> entities = jpaRepository.findAll();
        return entityMapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        return jpaRepository.existsByName(name.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        return jpaRepository.findByName(name.trim())
                .map(entityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByNameContainingIgnoreCase(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or blank");
        }

        List<RoleEntity> entities = jpaRepository.findByNameContainingIgnoreCase(pattern.trim());
        return entityMapper.toDomainList(entities);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jpaRepository.count();
    }
}
