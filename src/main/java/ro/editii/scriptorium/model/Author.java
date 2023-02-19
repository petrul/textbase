package ro.editii.scriptorium.model;

import java.io.*;
import java.sql.Blob;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;

import lombok.*;
import ro.editii.scriptorium.Util;

@Entity
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Author implements Comparable<Author> {

    final public static String ANONYMOUS_AUTHOR = "[ANON]"; // the bible for ex.

    // mapping  caragiale => Caragiale,Ion-Luca
    public static Properties RECOMMENDED_AUTHOR_MAPPINGS;

    // reverse mapping : Caragiale,Ion-Luca=>caragiale
    public static Properties RECOMMENDED_AUTHOR_MAPPINGS_REV = new Properties();

    // 'Regina Maria a României' => Author(strId = 'regina_maria' ...)
    public static Map<String, Author> SPECIAL_AUTHORS; // kings and such

    public static Set<String> FORBIDDEN_AUTHOR_NAMES = new HashSet<>();
    static {
        RECOMMENDED_AUTHOR_MAPPINGS = Util.readRecommendedAuthorMappings();
        RECOMMENDED_AUTHOR_MAPPINGS.keySet().forEach( (key) -> {
            String skey = (String) key;
            String value = RECOMMENDED_AUTHOR_MAPPINGS.getProperty(skey);
            RECOMMENDED_AUTHOR_MAPPINGS_REV.setProperty(value, skey);
        });

        FORBIDDEN_AUTHOR_NAMES = Util.readForbiddenAuthorNames();
        SPECIAL_AUTHORS = Util.readSpecialAuthorsResource();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * e.g. balcescu, should be all-lowercase, no-accent version of lastName
     */
    @Column(unique = true)
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
    @Column(unique = true)
    String originalNameInTeiFile;

    // prefered display name; if null, computed from firstName and lastName by method getVisualName()
    String displayName;

    String description; // remove

    @Lob @ToString.Exclude
    Blob avatar;


    /**
     * how to parse an originalNameInTeiFile : e.g. Alecsandri,Vasile.
     *
     * - First, should be the name, than the first name.
     * - for one name only, lastname gets it, first name stands empty
     * - for several commas (should never happen, first element goes as last name, the rest are reconcatenated using
     * blanks and go into the first name
     *
     * @param originalNameInTeiFile
     */
    public static Author newFromOriginalNameInTeiFile(String originalNameInTeiFile) {
        assert originalNameInTeiFile != null;

        originalNameInTeiFile = originalNameInTeiFile.trim();

        if (Author.SPECIAL_AUTHORS.containsKey(originalNameInTeiFile))
            return Author.SPECIAL_AUTHORS.get(originalNameInTeiFile);

        Author author = new Author();

        List<String> strings;
        if (originalNameInTeiFile.contains(",")) {
            strings = Arrays.asList(originalNameInTeiFile.split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            if (originalNameInTeiFile.contains(" ")) {
                strings = Arrays.asList(originalNameInTeiFile.split("\\s+"))
                        .stream()
                        .map(String::trim)
                        .collect(Collectors.toList());
                Collections.reverse(strings);
            } else {
                strings = new ArrayList<>();
                strings.add(originalNameInTeiFile);
            }
        }

        if (strings.size() < 1) {
            // the Bible and such
            author.setLastName(ANONYMOUS_AUTHOR);
        }

        String strings_0_trimmed = strings.get(0);
        if (strings.size() == 2) { // most common case
            author.lastName = strings_0_trimmed;
            if (author.lastName.equals(ANONYMOUS_AUTHOR))
                throw new IllegalArgumentException("last name of author should not be " + ANONYMOUS_AUTHOR);
            author.firstName = strings.get(1);

        } else
        if (strings.size() == 1) {
            // famous 1-named like "Plato"
            if (strings_0_trimmed.equals(ANONYMOUS_AUTHOR)
                    ||
                "NONE".equals(strings_0_trimmed.toUpperCase()))
                author.setLastName(ANONYMOUS_AUTHOR);
            else
                author.lastName = strings_0_trimmed;
        } else {
            // more than two names: first is last, the rest are joined as composed first name
            author.lastName = strings_0_trimmed;
            List<String> subarr = strings.subList(1, strings.size());
            author.firstName = String.join(" ", subarr).trim();
        }

        author.originalNameInTeiFile = recomposeOriginalFromParsedAuthor(author);

        // the strId is not set here. this is because we can only agree on a proper strId by looking
        // into the db. ParseTeiFileIntoDb#compute_strid_for_new_author

        //author.strId = Util.urlFriendify(author.lastName);


        return author;
    }


    protected static String recomposeOriginalFromParsedAuthor(Author author) {
        if (author.isAnonymous() || author.isOneNamed())
            return author.getLastName();
        else
            return author.getLastName() + "," + author.getFirstName();
    }

    /**
     * @return displayName if non-null, else computes visually appealing name from first and last names.
     */
    public String getVisualName() {
        if (this.displayName != null)
            return this.displayName;

        if (firstName == null)
            return this.lastName;
        else
            return this.firstName + " " + this.lastName;
    }

    public boolean isAnonymous() {
        return ANONYMOUS_AUTHOR.equals(this.lastName);
    }

    public boolean isOneNamed() {
        return this.lastName != null && this.lastName.length() > 0 && this.firstName == null;
    }

    public boolean isTwoNamed() {
        return this.lastName != null && this.lastName.length() > 0
                && this.firstName != null && this.firstName.length() > 0;
    }


    @Override
    public int compareTo(Author other) {
        final String thisLastName = this.getLastName();
        final String thatLastName = other.getLastName();

        if (thisLastName == null || thatLastName == null)
            return 0;

        int result = thisLastName.compareTo(thatLastName);
        if (result != 0) return result;

        final String thisFirstName = this.getFirstName();
        final String thatFirstName = other.getFirstName();

        if (thisFirstName == null || thatFirstName == null)
            return 0;
        result = thisFirstName.compareTo(thatFirstName);
        return result;
    }
}
