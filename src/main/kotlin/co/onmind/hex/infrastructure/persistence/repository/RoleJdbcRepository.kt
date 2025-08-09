package co.onmind.hex.infrastructure.persistence.repository

import co.onmind.hex.infrastructure.persistence.entity.RoleEntity
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.*

/**
 * JDBC Repository for Role entities.
 * 
 * This repository provides data access operations
 * using Micronaut Data JDBC.
 */
@Repository
@JdbcRepository(dialect = Dialect.H2)
interface RoleJdbcRepository : CrudRepository<RoleEntity, Long> {
    
    fun findByName(name: String): Optional<RoleEntity>
    
    @Query("SELECT * FROM roles WHERE UPPER(name) LIKE UPPER(:pattern)")
    fun findByNameContainingIgnoreCase(pattern: String): List<RoleEntity>
    
    fun existsByName(name: String): Boolean
}