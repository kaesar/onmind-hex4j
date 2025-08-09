package co.onmind.hex.domain.ports.out

import co.onmind.hex.domain.model.Role

interface RoleRepositoryPort {
    fun save(role: Role): Role
    fun findById(id: Long): Role?
    fun findByName(name: String): Role?
    fun findAll(): List<Role>
    fun findByNameContaining(pattern: String): List<Role>
    fun existsByName(name: String): Boolean
    fun deleteById(id: Long): Boolean
    fun count(): Long
}