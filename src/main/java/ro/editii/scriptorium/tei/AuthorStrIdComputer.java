package ro.editii.scriptorium.tei;

import org.springframework.stereotype.Component;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.model.Author;

import java.util.Optional;

/**
 * a layer of code over {@link CandidateStrIdGeneratorForAuthor}
 */
@Component
public class AuthorStrIdComputer {

    private final AuthorRepository authorRepository;

    public AuthorStrIdComputer(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /**
     *try a number of possibilities until smth acceptable comes around
     * @param author is necessarily a NEW author, it has already been looked up in the db
     */

    public String compute_strid_for_new_author(Author author) {

        CandidateStrIdGeneratorForAuthor candidates = new CandidateStrIdGeneratorForAuthor(author);

        for (String candidate : candidates) {

//            if (Author.FORBIDDEN_AUTHOR_NAMES.contains(candidate))
//                continue;

//            String entry = Author.RECOMMENDED_AUTHOR_MAPPINGS.getProperty(candidate);

//            if (entry == null) {
                // no recommandation for this candidate url fragment
                Optional<Author> optionalAuthor = this.authorRepository.getByStrId(candidate);
                if (optionalAuthor.isPresent()) {
                    // candidate strId already present in the db

                    // assert we really found someone else: if we're here, the author is supposed to be new, so a search has
                    // already been made for it by the originalNameInTeiFile
                    if (optionalAuthor.get().getOriginalNameInTeiFile()
                            .equals(author.getOriginalNameInTeiFile())) {
                        throw new IllegalStateException(String.format("expected to find a different author already existing in the db found %s, " +
                                " new candidate  is %s", optionalAuthor.get().toString(), author.toString()));
                    }

                    continue; // see next candidate
                } else {
                    /*
                        no existing author was found by that strId, so the proposed candidate is acceptable
                         and we will take it.
                     */
                    author.setStrId(candidate);
                    return candidate;
                }

//            } else {
//                // there is a recommandation by this url fragment
//                if (entry.equals(author.getOriginalNameInTeiFile())) {
//                    // the recommandation is meant for us : we are "the" caragiale_ionluca
//                    author.setStrId(candidate);
//                    return candidate;
//                } else {
//                    // the recommandation is not for us: we are caragiale mateiu, so try another candidate
//                    continue;
//                }
//            }
        }

        throw new IllegalStateException("should never get here, the iterator is infinite");
    }

}
