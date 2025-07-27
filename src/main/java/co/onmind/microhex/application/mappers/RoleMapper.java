package co.onmind.microhex.application.mappers;

import co.onmind.microhex.application.dto.in.CreateRoleRequestDto;
import co.onmind.microhex.application.dto.out.RoleResponseDto;
import co.onmind.microhex.domain.models.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Role domain model and DTOs.
 * 
 * This interface defines the mapping rules between the domain model and
 * the application layer DTOs. MapStruct will generate the implementation
 * at compile time, providing type-safe and performant mapping.
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    /**
     * Converts a Role domain model to a RoleResponseDto.
     * 
     * @param role The domain model to convert
     * @return The corresponding response DTO
     */
    RoleResponseDto toResponseDto(Role role);
    
    /**
     * Converts a CreateRoleRequestDto to a Role domain model.
     * The id and createdAt fields will be null and should be set by the domain service.
     * 
     * @param createRoleRequestDto The request DTO to convert
     * @return The corresponding domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Role toDomainModel(CreateRoleRequestDto createRoleRequestDto);
}