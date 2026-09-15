package co.onmind.hex.application.mappers;

import co.onmind.hex.application.dto.out.ScriptResultResponseDto;
import co.onmind.hex.domain.models.ScriptResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScriptingMapper {

    default ScriptResultResponseDto toResponseDto(ScriptResult result) {
        if (result == null) {
            return null;
        }
        return new ScriptResultResponseDto(result.value(), result.stdout(), result.stderr());
    }
}
