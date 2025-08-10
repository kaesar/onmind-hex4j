package co.onmind.microhex.application.mappers;

import co.onmind.microhex.application.dto.CreateRoleRequest;
import co.onmind.microhex.application.dto.RoleResponse;
import co.onmind.microhex.domain.models.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for converting between Role domain models and DTOs.
 * 
 * This mapper uses MapStruct to automatically generate the implementation
 * at compile time, providing better performance and type safety.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 2.0.0
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);
    
    /**
     * Converts a Role domain model to RoleResponse DTO.
     * @param role the domain model
     * @return the response DTO
     */
    RoleResponse toResponse(Role role);
    
    /**
     * Converts a list of Role domain models to a list of RoleResponse DTOs.
     * @param roles the list of domain models
     * @return the list of response DTOs
     */
    List<RoleResponse> toResponseList(List<Role> roles);
    
    /**
     * Converts a CreateRoleRequest DTO to Role domain model.
     * Maps only the name field, ignoring id and createdAt.
     * @param request the create request DTO
     * @return the domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "withName", ignore = true)
    Role toDomain(CreateRoleRequest request);
}