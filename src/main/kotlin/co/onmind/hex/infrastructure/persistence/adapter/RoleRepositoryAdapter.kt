package co.onmind.hex.infrastructure.persistence.adapter

import co.onmind.hex.domain.model.Role
import co.onmind.hex.domain.ports.out.RoleRepositoryPort
import co.onmind.hex.infrastructure.persistence.entity.RoleEntity
import co.onmind.hex.infrastructure.persistence.repository.RoleJdbcRepository
import jakarta.inject.Singleton

/**
 * Adapter that implements the RoleRepository port using JDBC.
 * 
 * This adapter bridges the domain layer with the infrastructure layer,
 * converting between domain models and persistence entities.
 */
@Singleton
class RoleRepositoryAdapter(
    private val jdbcRepository: RoleJdbcRepository
) : RoleRepositoryPort {
    
    override fun save(role: Role): Role {
        val entity = toEntity(role)
        val savedEntity = jdbcRepository.save(entity)
        return toDomain(savedEntity)
    }
    
    override fun findById(id: Long): Role? {
        return jdbcRepository.findById(id)
            .map { toDomain(it) }
            .orElse(null)
    }
    
    override fun findByName(name: String): Role? {
        return jdbcRepository.findByName(name)
            .map { toDomain(it) }
            .orElse(null)
    }
    
    override fun findAll(): List<Role> {
        return jdbcRepository.findAll()
            .map { toDomain(it) }
    }
    
    override fun findByNameContaining(pattern: String): List<Role> {
        val searchPattern = "%$pattern%"
        return jdbcRepository.findByNameContainingIgnoreCase(searchPattern)
            .map { toDomain(it) }
    }
    
    override fun deleteById(id: Long): Boolean {
        return if (jdbcRepository.existsById(id)) {
            jdbcRepository.deleteById(id)
            true
        } else {
            false
        }
    }
    
    override fun existsByName(name: String): Boolean {
        return jdbcRepository.existsByName(name)
    }
    
    override fun count(): Long {
        return jdbcRepository.count()
    }
    
    // ========== Mapping Methods ==========
    
    private fun toDomain(entity: RoleEntity): Role {
        return Role(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt
        )
    }
    
    private fun toEntity(role: Role): RoleEntity {
        return RoleEntity(
            id = role.id,
            name = role.name,
            createdAt = role.createdAt
        )
    }
}