package ro.editii.scriptorium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.service.AuthorsAndTeiDivs;

import java.util.List;
import java.util.stream.Collectors;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthorsAndTeiDivsDto {
    List<AuthorDto> authors;
    List<TeiDivDto> divs;

    public static AuthorsAndTeiDivsDto from(AuthorsAndTeiDivs arg, UriComponentsBuilder uriComponentsBuilder) {
        List<AuthorDto> authorDtos = arg.getAuthors().stream()
                .map(it -> AuthorDto.from(it))
                .collect(Collectors.toList());
        List<TeiDivDto> teiDivDtos = arg.getTeiDivs().stream()
                .map(it -> TeiDivDto.fromTeiDiv(it, uriComponentsBuilder))
                .collect(Collectors.toList());
        return AuthorsAndTeiDivsDto.builder()
                .authors(authorDtos)
                .divs(teiDivDtos)
                .build();
    }
}
