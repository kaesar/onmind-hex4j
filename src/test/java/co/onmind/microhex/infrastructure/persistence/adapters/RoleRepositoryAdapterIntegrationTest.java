package co.onmind.microhex.infrastructure.persistence.adapters;

import co.onmind.microhex.MicroHexApplication;
import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.infrastructure.persistence.mappers.RoleEntityMapper;
import co.onmind.microhex.infrastructure.persistence.repositories.JpaRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RoleRepositoryAdapter.
 * 
 * This test class uses @SpringBootTest to test the complete integration
 * between the adapter, JPA repository, entity mapper, and H2 database.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@SpringBootTest(classes = MicroHexApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.database=h2",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Transactional
@DisplayName("Role Repository Adapter Integration Tests")
class RoleRepositoryAdapterIntegrationTest {
    
    @Autowired
    private RoleRepositoryAdapter repositoryAdapter;
    
    @Autowired
    private JpaRoleRepository jpaRepository;
    
    @Autowired
    private RoleEntityMapper entityMapper;
    
    @BeforeEach
    void setUp() {
        // Clean up database before each test
        jpaRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should save and retrieve role successfully")
    void shouldSaveAndRetrieveRoleSuccessfully() {
        // Given
        Role newRole = new Role("ADMIN");
        
        // When
        Role savedRole = repositoryAdapter.save(newRole);
        
        // Then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("ADMIN");
        assertThat(savedRole.getCreatedAt()).isNotNull();
        
        // Verify it was actually saved to database
        Optional<Role> retrievedRole = repositoryAdapter.findById(savedRole.getId());
        assertThat(retrievedRole).isPresent();
        assertThat(retrievedRole.get().getName()).isEqualTo("ADMIN");
    }
    
    @Test
    @DisplayName("Should find role by name")
    void shouldFindRoleByName() {
        // Given
        Role savedRole = repositoryAdapter.save(new Role("USER"));
        
        // When
        Optional<Role> foundRole = repositoryAdapter.findByName("USER");
        
        // Then
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getId()).isEqualTo(savedRole.getId());
        assertThat(foundRole.get().getName()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("Should check if role exists by name")
    void shouldCheckIfRoleExistsByName() {
        // Given
        repositoryAdapter.save(new Role("MODERATOR"));
        
        // When & Then
        assertThat(repositoryAdapter.existsByName("MODERATOR")).isTrue();
        assertThat(repositoryAdapter.existsByName("NONEXISTENT")).isFalse();
    }
    
    @Test
    @DisplayName("Should find all roles")
    void shouldFindAllRoles() {
        // Given
        repositoryAdapter.save(new Role("ADMIN"));
        repositoryAdapter.save(new Role("USER"));
        repositoryAdapter.save(new Role("MODERATOR"));
        
        // When
        List<Role> allRoles = repositoryAdapter.findAll();
        
        // Then
        assertThat(allRoles).hasSize(3);
        assertThat(allRoles).extracting(Role::getName)
                .containsExactlyInAnyOrder("ADMIN", "USER", "MODERATOR");
    }
    
    @Test
    @DisplayName("Should update existing role")
    void shouldUpdateExistingRole() {
        // Given
        Role savedRole = repositoryAdapter.save(new Role("ADMIN"));
        
        // When
        savedRole.setName("SUPER_ADMIN");
        Role updatedRole = repositoryAdapter.save(savedRole);
        
        // Then
        assertThat(updatedRole.getId()).isEqualTo(savedRole.getId());
        assertThat(updatedRole.getName()).isEqualTo("SUPER_ADMIN");
        
        // Verify in database
        Optional<Role> retrievedRole = repositoryAdapter.findById(savedRole.getId());
        assertThat(retrievedRole).isPresent();
        assertThat(retrievedRole.get().getName()).isEqualTo("SUPER_ADMIN");
    }
    
    @Test
    @DisplayName("Should delete role by id")
    void shouldDeleteRoleById() {
        // Given
        Role savedRole = repositoryAdapter.save(new Role("TEMP_ROLE"));
        Long roleId = savedRole.getId();
        
        // When
        repositoryAdapter.deleteById(roleId);
        
        // Then
        Optional<Role> deletedRole = repositoryAdapter.findById(roleId);
        assertThat(deletedRole).isEmpty();
    }
    
    @Test
    @DisplayName("Should count roles")
    void shouldCountRoles() {
        // Given
        repositoryAdapter.save(new Role("ROLE1"));
        repositoryAdapter.save(new Role("ROLE2"));
        
        // When
        long count = repositoryAdapter.count();
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should handle case insensitive operations")
    void shouldHandleCaseInsensitiveOperations() {
        // Given
        repositoryAdapter.save(new Role("ADMIN"));
        
        // When & Then
        assertThat(repositoryAdapter.existsByNameIgnoreCase("admin")).isTrue();
        assertThat(repositoryAdapter.existsByNameIgnoreCase("ADMIN")).isTrue();
        assertThat(repositoryAdapter.existsByNameIgnoreCase("Admin")).isTrue();
        
        Optional<Role> foundRole = repositoryAdapter.findByNameIgnoreCase("admin");
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ADMIN");
    }
    
    @Test
    @DisplayName("Should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        // When & Then
        assertThat(repositoryAdapter.findById(999L)).isEmpty();
        assertThat(repositoryAdapter.findByName("NONEXISTENT")).isEmpty();
        assertThat(repositoryAdapter.existsByName("NONEXISTENT")).isFalse();
        assertThat(repositoryAdapter.findAll()).isEmpty();
        assertThat(repositoryAdapter.count()).isEqualTo(0);
    }
}