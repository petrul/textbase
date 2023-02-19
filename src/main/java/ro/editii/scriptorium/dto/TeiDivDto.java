package ro.editii.scriptorium.dto;

import lombok.*;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.model.TeiDiv;

@Data
@NoArgsConstructor
@ToString
public class TeiDivDto {
    String path;
    String urlFragment;
    String head;
    String url;
    int depth;

    int size;
    int wordSize;

    TeiDivDto[] children;
    TeiDivDto parent;

    @Builder(builderMethodName = "teiDivDtoBuilder")
    public TeiDivDto(String path, String urlFragment, String head,  String url, int depth,
                     int size, int wordSize,
                     TeiDivDto[] children,
                     TeiDivDto parent) {
        this.path = path;
        this.urlFragment = urlFragment;
        this.head = head;
        this.url = url;
        this.depth = depth;
        this.children = children;
        this.parent = parent;
        this.size = size;
        this.wordSize = wordSize;
    }

    public static TeiDivDto fromTeiDiv(TeiDiv teiDiv, UriComponentsBuilder uriComponentsBuilder) {
        return fromTeiDiv(teiDiv, uriComponentsBuilder.path("/").toUriString());
    }

    public static TeiDivDto fromTeiDiv(TeiDiv teiDiv, String baseUrl) {
        TeiDivDto dto = new TeiDivDto();
        dto.url = baseUrl + teiDiv.getAuthor().getStrId() + "/" + teiDiv.getUrl();
        dto.head = teiDiv.getHead();
        dto.depth = teiDiv.getDepth();
        dto.size = teiDiv.getSize();
        dto.wordSize = teiDiv.getWordSize();

        return  dto;
    }
}
