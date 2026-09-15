package co.onmind.hex.infrastructure.handlers;

import co.onmind.hex.application.dto.out.SheetResponseDto;
import co.onmind.hex.application.ports.in.AbcSheetTrait;
import co.onmind.hex.application.ports.in.AbcSheetTrait.SheetRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@ConditionalOnProperty(name = "app.graphql.enabled", havingValue = "true")
public class AbcGraphqlResolver {

    private final AbcSheetTrait abcSheetTrait;

    public AbcGraphqlResolver(AbcSheetTrait abcSheetTrait) {
        this.abcSheetTrait = abcSheetTrait;
    }

    @QueryMapping
    public SheetResponseDto abcSheet(
            @Argument String show,
            @Argument String from,
            @Argument String some) {
        return abcSheetTrait.sheet(show, from, some);
    }

    @QueryMapping
    public List<SheetResponseDto> abcSheets(@Argument List<AbcSheetInput> requests) {
        List<SheetRequest> sheetRequests = new ArrayList<>();
        for (AbcSheetInput input : requests) {
            sheetRequests.add(new SheetRequest(input.show(), input.from(), input.some()));
        }
        return abcSheetTrait.sheets(sheetRequests);
    }

    public record AbcSheetInput(String show, String from, String some) {}
}
