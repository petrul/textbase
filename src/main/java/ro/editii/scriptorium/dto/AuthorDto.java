package ro.editii.scriptorium.dto;

import lombok.*;
import ro.editii.scriptorium.model.Author;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthorDto {

    /**
     * e.g. balcescu, should be all-lowercase, no-accent version of lastName
     */
    String strId;

    /**
     * Alecsandri
     */
    @EqualsAndHashCode.Include
    String lastName;

    /**
     * e.g. Vasile
     */
    @EqualsAndHashCode.Include
    String firstName;

    /**
     * e.g. "Alecsandri,Vasile". the file name as in the TEI. maybe useful for identifying authors
     * that have already been inserted
     */
    String originalNameInTeiFile;

    // prefered display name; if null, computed from firstName and lastName by method getVisualName()
    String displayName;
    String description; // remove
    OpusDto[] opera;
    String image_href;

    public static AuthorDto from(Author author) {
        return AuthorDto.builder()
                .strId(author.getStrId())
                .lastName(author.getLastName())
                .firstName(author.getFirstName())
                .originalNameInTeiFile(author.getOriginalNameInTeiFile())
                .displayName(author.getDisplayName())
                .description(author.getDescription())
                .build();
    }
}
