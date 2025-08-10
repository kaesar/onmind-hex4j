package co.onmind.microhex.infrastructure.persistence.adapters;

import co.onmind.microhex.domain.models.Role;
import co.onmind.microhex.infrastructure.persistence.entities.RoleEntity;
import co.onmind.microhex.infrastructure.persistence.mappers.RoleEntityMapper;
import co.onmind.microhex.infrastructure.persistence.repositories.JpaRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleRepositoryAdapter.
 * 
 * This test class focuses on testing the adapter logic and the interaction
 * between the adapter, JPA repository, and entity mapper. It uses mocks
 * to isolate the adapter from its dependencies.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Repository Adapter Tests")
class RoleRepositoryAdapterTest {
    
    @Mock
    private JpaRoleRepository jpaRepository;
    
    @Mock
    private RoleEntityMapper entityMapper;
    
    @InjectMocks
    private RoleRepositoryAdapter repositoryAdapter;
    
    private Role testRole;
    private RoleEntity testEntity;
    private LocalDateTime testTime;
    
    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        testRole = new Role(1L, "ADMIN", testTime);
        testEntity = new RoleEntity(1L, "ADMIN", testTime);
    }
    
    @Test
    @DisplayName("Should save new role successfully")
    void shouldSaveNewRoleSuccessfully() {
        // Given
        Role newRole = new Role("USER"); // No ID - new role
        RoleEntity newEntity = new RoleEntity("USER");
        RoleEntity savedEntity = new RoleEntity(2L, "USER", testTime);
        Role savedRole = new Role(2L, "USER", testTime);
        
        when(entityMapper.toNewEntity(newRole)).thenReturn(newEntity);
        when(jpaRepository.save(newEntity)).thenReturn(savedEntity);
        when(entityMapper.toDomain(savedEntity)).thenReturn(savedRole);
        
        // When
        Role result = repositoryAdapter.save(newRole);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("USER");
        
        verify(entityMapper).toNewEntity(newRole);
        verify(jpaRepository).save(newEntity);
        verify(entityMapper).toDomain(savedEntity);
    }
    
    @Test
    @DisplayName("Should update existing role successfully")
    void shouldUpdateExistingRoleSuccessfully() {
        // Given
        Role existingRole = new Role(1L, "ADMIN_UPDATED", testTime);
        RoleEntity existingEntity = new RoleEntity(1L, "ADMIN_UPDATED", testTime);
        RoleEntity savedEntity = new RoleEntity(1L, "ADMIN_UPDATED", testTime);
        Role savedRole = new Role(1L, "ADMIN_UPDATED", testTime);
        
        when(entityMapper.toEntity(existingRole)).thenReturn(existingEntity);
        when(jpaRepository.save(existingEntity)).thenReturn(savedEntity);
        when(entityMapper.toDomain(savedEntity)).thenReturn(savedRole);
        
        // When
        Role result = repositoryAdapter.save(existingRole);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ADMIN_UPDATED");
        
        verify(entityMapper).toEntity(existingRole);
        verify(jpaRepository).save(existingEntity);
        verify(entityMapper).toDomain(savedEntity);
    }
    
    @Test
    @DisplayName("Should throw exception when saving null role")
    void shouldThrowExceptionWhenSavingNullRole() {
        // When & Then
        assertThatThrownBy(() -> repositoryAdapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null");
        
        verifyNoInteractions(entityMapper, jpaRepository);
    }
    
    @Test
    @DisplayName("Should find role by id successfully")
    void shouldFindRoleByIdSuccessfully() {
        // Given
        Long roleId = 1L;
        when(jpaRepository.findById(roleId)).thenReturn(Optional.of(testEntity));
        when(entityMapper.toDomain(testEntity)).thenReturn(testRole);
        
        // When
        Optional<Role> result = repositoryAdapter.findById(roleId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRole);
        
        verify(jpaRepository).findById(roleId);
        verify(entityMapper).toDomain(testEntity);
    }
    
    @Test
    @DisplayName("Should return empty when role not found by id")
    void shouldReturnEmptyWhenRoleNotFoundById() {
        // Given
        Long roleId = 999L;
        when(jpaRepository.findById(roleId)).thenReturn(Optional.empty());
        
        // When
        Optional<Role> result = repositoryAdapter.findById(roleId);
        
        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findById(roleId);
        verifyNoInteractions(entityMapper);
    }
    
    @Test
    @DisplayName("Should throw exception when finding by null id")
    void shouldThrowExceptionWhenFindingByNullId() {
        // When & Then
        assertThatThrownBy(() -> repositoryAdapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null");
        
        verifyNoInteractions(jpaRepository, entityMapper);
    }
    
    @Test
    @DisplayName("Should find all roles successfully")
    void shouldFindAllRolesSuccessfully() {
        // Given
        RoleEntity entity2 = new RoleEntity(2L, "USER", testTime);
        List<RoleEntity> entities = Arrays.asList(testEntity, entity2);
        
        Role role2 = new Role(2L, "USER", testTime);
        List<Role> roles = Arrays.asList(testRole, role2);
        
        when(jpaRepository.findAll()).thenReturn(entities);
        when(entityMapper.toDomainList(entities)).thenReturn(roles);
        
        // When
        List<Role> result = repositoryAdapter.findAll();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testRole, role2);
        
        verify(jpaRepository).findAll();
        verify(entityMapper).toDomainList(entities);
    }
    
    @Test
    @DisplayName("Should check if role exists by name")
    void shouldCheckIfRoleExistsByName() {
        // Given
        String roleName = "ADMIN";
        when(jpaRepository.existsByName(roleName)).thenReturn(true);
        
        // When
        boolean result = repositoryAdapter.existsByName(roleName);
        
        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByName(roleName);
    }
    
    @Test
    @DisplayName("Should trim name when checking existence")
    void shouldTrimNameWhenCheckingExistence() {
        // Given
        String roleName = "  ADMIN  ";
        when(jpaRepository.existsByName("ADMIN")).thenReturn(true);
        
        // When
        boolean result = repositoryAdapter.existsByName(roleName);
        
        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByName("ADMIN");
    }
    
    @Test
    @DisplayName("Should throw exception when checking existence with null name")
    void shouldThrowExceptionWhenCheckingExistenceWithNullName() {
        // When & Then
        assertThatThrownBy(() -> repositoryAdapter.existsByName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or blank");
        
        verifyNoInteractions(jpaRepository);
    }
    
    @Test
    @DisplayName("Should throw exception when checking existence with blank name")
    void shouldThrowExceptionWhenCheckingExistenceWithBlankName() {
        // When & Then
        assertThatThrownBy(() -> repositoryAdapter.existsByName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or blank");
        
        verifyNoInteractions(jpaRepository);
    }
    
    @Test
    @DisplayName("Should find role by name successfully")
    void shouldFindRoleByNameSuccessfully() {
        // Given
        String roleName = "ADMIN";
        when(jpaRepository.findByName(roleName)).thenReturn(Optional.of(testEntity));
        when(entityMapper.toDomain(testEntity)).thenReturn(testRole);
        
        // When
        Optional<Role> result = repositoryAdapter.findByName(roleName);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRole);
        
        verify(jpaRepository).findByName(roleName);
        verify(entityMapper).toDomain(testEntity);
    }
    
    @Test
    @DisplayName("Should return empty when role not found by name")
    void shouldReturnEmptyWhenRoleNotFoundByName() {
        // Given
        String roleName = "NONEXISTENT";
        when(jpaRepository.findByName(roleName)).thenReturn(Optional.empty());
        
        // When
        Optional<Role> result = repositoryAdapter.findByName(roleName);
        
        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findByName(roleName);
        verifyNoInteractions(entityMapper);
    }
    
    @Test
    @DisplayName("Should delete role by id successfully")
    void shouldDeleteRoleByIdSuccessfully() {
        // Given
        Long roleId = 1L;
        
        when(jpaRepository.existsById(roleId)).thenReturn(true);
        
        // When
        boolean result = repositoryAdapter.deleteById(roleId);
        
        // Then
        assertTrue(result);
        verify(jpaRepository).existsById(roleId);
        verify(jpaRepository).deleteById(roleId);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting with null id")
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // When & Then
        assertThatThrownBy(() -> repositoryAdapter.deleteById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be null");
        
        verifyNoInteractions(jpaRepository);
    }
    
    @Test
    @DisplayName("Should count roles successfully")
    void shouldCountRolesSuccessfully() {
        // Given
        when(jpaRepository.count()).thenReturn(5L);
        
        // When
        long result = repositoryAdapter.count();
        
        // Then
        assertThat(result).isEqualTo(5L);
        verify(jpaRepository).count();
    }
    
    @Test
    @DisplayName("Should check if role exists by name ignoring case")
    void shouldCheckIfRoleExistsByNameIgnoringCase() {
        // Given
        String roleName = "admin";
        when(jpaRepository.existsByNameIgnoreCase("admin")).thenReturn(true);
        
        // When
        boolean result = repositoryAdapter.existsByNameIgnoreCase(roleName);
        
        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByNameIgnoreCase("admin");
    }
    
    @Test
    @DisplayName("Should find role by name ignoring case")
    void shouldFindRoleByNameIgnoringCase() {
        // Given
        String roleName = "admin";
        when(jpaRepository.findByNameIgnoreCase("admin")).thenReturn(Optional.of(testEntity));
        when(entityMapper.toDomain(testEntity)).thenReturn(testRole);
        
        // When
        Optional<Role> result = repositoryAdapter.findByNameIgnoreCase(roleName);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRole);
        
        verify(jpaRepository).findByNameIgnoreCase("admin");
        verify(entityMapper).toDomain(testEntity);
    }
}