package co.onmind.hex.application.ports.out;

import co.onmind.hex.domain.models.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {

    Role save(Role role);

    Optional<Role> findById(Long id);

    List<Role> findAll();

    boolean existsByName(String name);

    Optional<Role> findByName(String name);

    List<Role> findByNameContainingIgnoreCase(String namePattern);

    void deleteById(Long id);

    long count();
}
