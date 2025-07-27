package co.onmind.microhex.infrastructure.persistence.repositories;

import co.onmind.microhex.infrastructure.persistence.entities.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for JpaRoleRepository.
 * 
 * This test class uses @DataJpaTest to test the JPA repository layer
 * with an in-memory H2 database. It focuses on testing the custom
 * query methods and ensuring proper database interactions.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@DataJpaTest
@DisplayName("JPA Role Repository Tests")
class JpaRoleRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private JpaRoleRepository roleRepository;
    
    private RoleEntity testRole1;
    private RoleEntity testRole2;
    
    @BeforeEach
    void setUp() {
        // Clear any existing data
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // Create test data
        testRole1 = new RoleEntity("ADMIN");
        testRole2 = new RoleEntity("USER");
        
        // Persist test data
        testRole1 = entityManager.persistAndFlush(testRole1);
        testRole2 = entityManager.persistAndFlush(testRole2);
        entityManager.clear();
    }
    
    @Test
    @DisplayName("Should find role by name")
    void shouldFindRoleByName() {
        // When
        Optional<RoleEntity> found = roleRepository.findByName("ADMIN");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ADMIN");
        assertThat(found.get().getId()).isEqualTo(testRole1.getId());
    }
    
    @Test
    @DisplayName("Should return empty when role name not found")
    void shouldReturnEmptyWhenRoleNameNotFound() {
        // When
        Optional<RoleEntity> found = roleRepository.findByName("NONEXISTENT");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("Should check if role exists by name")
    void shouldCheckIfRoleExistsByName() {
        // When & Then
        assertThat(roleRepository.existsByName("ADMIN")).isTrue();
        assertThat(roleRepository.existsByName("USER")).isTrue();
        assertThat(roleRepository.existsByName("NONEXISTENT")).isFalse();
    }
    
    @Test
    @DisplayName("Should find role by name ignoring case")
    void shouldFindRoleByNameIgnoringCase() {
        // When
        Optional<RoleEntity> found1 = roleRepository.findByNameIgnoreCase("admin");
        Optional<RoleEntity> found2 = roleRepository.findByNameIgnoreCase("ADMIN");
        Optional<RoleEntity> found3 = roleRepository.findByNameIgnoreCase("Admin");
        
        // Then
        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found3).isPresent();
        
        assertThat(found1.get().getName()).isEqualTo("ADMIN");
        assertThat(found2.get().getName()).isEqualTo("ADMIN");
        assertThat(found3.get().getName()).isEqualTo("ADMIN");
    }
    
    @Test
    @DisplayName("Should check if role exists by name ignoring case")
    void shouldCheckIfRoleExistsByNameIgnoringCase() {
        // When & Then
        assertThat(roleRepository.existsByNameIgnoreCase("admin")).isTrue();
        assertThat(roleRepository.existsByNameIgnoreCase("ADMIN")).isTrue();
        assertThat(roleRepository.existsByNameIgnoreCase("Admin")).isTrue();
        assertThat(roleRepository.existsByNameIgnoreCase("user")).isTrue();
        assertThat(roleRepository.existsByNameIgnoreCase("nonexistent")).isFalse();
    }
    
    @Test
    @DisplayName("Should find roles by name containing pattern")
    void shouldFindRolesByNameContainingPattern() {
        // Given - add more test data
        RoleEntity managerRole = entityManager.persistAndFlush(new RoleEntity("MANAGER"));
        RoleEntity supervisorRole = entityManager.persistAndFlush(new RoleEntity("SUPERVISOR"));
        entityManager.clear();
        
        // When
        List<RoleEntity> rolesWithA = roleRepository.findByNameContaining("A");
        List<RoleEntity> rolesWithER = roleRepository.findByNameContaining("ER");
        List<RoleEntity> rolesWithXYZ = roleRepository.findByNameContaining("XYZ");
        
        // Then
        assertThat(rolesWithA).hasSize(3); // ADMIN, MANAGER, SUPERVISOR
        assertThat(rolesWithA).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("ADMIN", "MANAGER", "SUPERVISOR");
        
        assertThat(rolesWithER).hasSize(2); // MANAGER, SUPERVISOR
        assertThat(rolesWithER).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("MANAGER", "SUPERVISOR");
        
        assertThat(rolesWithXYZ).isEmpty();
    }
    
    @Test
    @DisplayName("Should count roles created after specific date")
    void shouldCountRolesCreatedAfterSpecificDate() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        // When
        long countAfterYesterday = roleRepository.countRolesCreatedAfter(yesterday);
        long countAfterTomorrow = roleRepository.countRolesCreatedAfter(tomorrow);
        
        // Then
        assertThat(countAfterYesterday).isEqualTo(2); // Both test roles created today
        assertThat(countAfterTomorrow).isEqualTo(0); // No roles created in the future
    }
    
    @Test
    @DisplayName("Should save and retrieve role with all fields")
    void shouldSaveAndRetrieveRoleWithAllFields() {
        // Given
        RoleEntity newRole = new RoleEntity("MODERATOR");
        
        // When
        RoleEntity saved = roleRepository.save(newRole);
        entityManager.flush();
        entityManager.clear();
        
        Optional<RoleEntity> retrieved = roleRepository.findById(saved.getId());
        
        // Then
        assertThat(retrieved).isPresent();
        RoleEntity role = retrieved.get();
        assertThat(role.getId()).isNotNull();
        assertThat(role.getName()).isEqualTo("MODERATOR");
        assertThat(role.getCreatedAt()).isNotNull();
        assertThat(role.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
    
    @Test
    @DisplayName("Should handle unique constraint on role name")
    void shouldHandleUniqueConstraintOnRoleName() {
        // Given
        RoleEntity duplicateRole = new RoleEntity("ADMIN"); // Same name as testRole1
        
        // When & Then
        try {
            roleRepository.save(duplicateRole);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).as("Expected unique constraint violation").isTrue();
        } catch (Exception e) {
            // Expected behavior - unique constraint violation
            assertThat(e).isNotNull();
        }
    }
    
    @Test
    @DisplayName("Should delete role by id")
    void shouldDeleteRoleById() {
        // Given
        Long roleId = testRole1.getId();
        assertThat(roleRepository.existsById(roleId)).isTrue();
        
        // When
        roleRepository.deleteById(roleId);
        entityManager.flush();
        
        // Then
        assertThat(roleRepository.existsById(roleId)).isFalse();
        assertThat(roleRepository.findById(roleId)).isEmpty();
    }
    
    @Test
    @DisplayName("Should count total roles")
    void shouldCountTotalRoles() {
        // When
        long count = roleRepository.count();
        
        // Then
        assertThat(count).isEqualTo(2); // testRole1 and testRole2
    }
    
    @Test
    @DisplayName("Should find all roles")
    void shouldFindAllRoles() {
        // When
        List<RoleEntity> allRoles = roleRepository.findAll();
        
        // Then
        assertThat(allRoles).hasSize(2);
        assertThat(allRoles).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }
    
    @Test
    @DisplayName("Should handle PrePersist callback for creation timestamp")
    void shouldHandlePrePersistCallbackForCreationTimestamp() {
        // Given
        RoleEntity newRole = new RoleEntity();
        newRole.setName("CALLBACK_TEST");
        // Don't set createdAt - should be set by @PrePersist
        
        // When
        RoleEntity saved = roleRepository.save(newRole);
        entityManager.flush();
        
        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
}