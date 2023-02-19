package ro.editii.scriptorium.dto;

import lombok.*;
import ro.editii.scriptorium.model.TeiDiv;

/**
 * 'Root' TeiDiv corresponding to a full work (top-level teidiv) of an author.
 */
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpusDto extends TeiDivDto {
    AuthorDto[] authors;

    @Builder(builderMethodName = "opusDtoBuilder")
    public OpusDto(String path, String urlFragment, String head, String url, int depth, int size, int wordSize,
                   TeiDivDto[] children, TeiDivDto parent, AuthorDto[] authors) {
        super(path, urlFragment, head, url, depth, size, wordSize, children, parent);
        this.authors = authors;
    }

    public static OpusDto from(TeiDiv it) {
        assert it.getParent() == null;
        return OpusDto.opusDtoBuilder()
                .head(it.getHead())
                .path(it.getCompletePath())
                .urlFragment(it.getUrlFragment())
                .size(it.getSize())
                .wordSize(it.getWordSize())
                .build();
    }
}
