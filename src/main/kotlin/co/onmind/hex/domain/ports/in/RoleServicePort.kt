package co.onmind.hex.domain.ports.`in`

import co.onmind.hex.domain.model.Role

interface RoleServicePort {
    fun createRole(name: String): Role
    fun updateRole(id: Long, newName: String): Role
    fun deleteRole(id: Long)
    fun getRoleById(id: Long): Role?
    fun getRoleByName(name: String): Role?
    fun getAllRoles(): List<Role>
    fun searchRolesByName(pattern: String): List<Role>
    fun getRoleCount(): Long
    fun roleExists(name: String): Boolean
}