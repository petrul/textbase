package ro.editii.scriptorium.tei;


import ro.editii.scriptorium.Util;
import ro.editii.scriptorium.model.Author;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * use this as a _neverending_ stream of propositions of strIds for an Author
 * for example, given an Author {
 * strId = 'alecsandri',
 * firstNme = 'Vasile'
 * }
 * <p>
 * using this class will generate:
 * 'alecsandri', 'alecsandri_vasile', 'alecsandri_vasile_2', 'alecsandri_vasile_3' ...
 * }
 */
public class CandidateStrIdGeneratorForAuthor implements Iterable<String> {

    Author author;

    public CandidateStrIdGeneratorForAuthor(Author author) {
        this.author = author;
    }

    @Override
    public Iterator<String> iterator() {
        return new CandidateStrIdGeneratorForAuthor_Iterator(this.author);
    }
}

/**
 * generates a succession of strId propositions, given an Author's name
 */
class CandidateStrIdGeneratorForAuthor_Iterator implements Iterator<String> {

    List<String> firstPropositions = new ArrayList<String>();
    Iterator<String> iterator;

    String lastProposition;
    int counter = 1;

    boolean hasNext = true;
    Author author;

    protected boolean isSpecialAuthor() {
        return Author.SPECIAL_AUTHORS.values().stream().anyMatch( it -> it.equals(this.author));
    }

    CandidateStrIdGeneratorForAuthor_Iterator(Author author) {
        this.author = author;
        String last_name_lower = Util.urlFriendify(author.getLastName());

        /* author is specified as special author */
        if (this.isSpecialAuthor()) {
            assert author.getStrId() != null;
            firstPropositions.add(author.getStrId());
        } else {
            /* last name is the first proposition in all cases except when another author with the same last name
             * was recommended in the resource file.
             */
            if (! isForbidden(last_name_lower) && !anotherAuthorWithSameStrIdHasRecommandation(last_name_lower)) {
                firstPropositions.add(last_name_lower);
            }

            if (author.getFirstName() != null)
                firstPropositions.add(last_name_lower + "_" + Util.urlFriendify(author.getFirstName()));

        }

        this.lastProposition = this.firstPropositions.get(0);
        this.iterator = firstPropositions.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public String next() {

        if (this.isSpecialAuthor()) { // only reaturn one value, specified in resource file special-authors.properties
            this.hasNext = false;
            return this.author.getStrId();
        }

        String maybeRecommendation = Author.RECOMMENDED_AUTHOR_MAPPINGS_REV.getProperty(this.author.getOriginalNameInTeiFile());
        if (maybeRecommendation != null) {
            // if there is a recommendation for this author, this iterator
            // will only return one compulsory value, the first.
            this.hasNext = false;
            return maybeRecommendation;
        }

        String proposition = null;
        do {
            if (this.iterator.hasNext()) {
                this.lastProposition = this.iterator.next();
                proposition = this.lastProposition;
            } else {
                proposition = this.lastProposition + "_" + (++this.counter);
            }
        } while (this.isForbidden(proposition));

        return proposition;
    }

    /**
     * some strId's (like /css, /js are protected from being author's names)
     */
    protected boolean isForbidden(String strId) {
        return Author.FORBIDDEN_AUTHOR_NAMES.contains(strId);
    }


    /**
     * does this author have special recommandation
     * in the {recommended-author-urls.properties} resource file.
     */
    protected boolean hasRecommandation(String strId) {
        String recom = Author.RECOMMENDED_AUTHOR_MAPPINGS.getProperty(strId);
        return (recom != null && recom.equals(this.author.getOriginalNameInTeiFile()));
    }


    /**
     * return true if there is a recommended author with same strId but not this one.
     * For example it would return true when computing strId candidates for Mateiu Caragiale
     * while the official 'recommended' Caragiale is Ion-Luca.
     */
    protected boolean anotherAuthorWithSameStrIdHasRecommandation(String strId) {
        String recommended = Author.RECOMMENDED_AUTHOR_MAPPINGS.getProperty(strId);
        if (recommended == null)
            return false;
        final Author that = Author.newFromOriginalNameInTeiFile(recommended);
        return (recommended != null && ! that.equals(this.author));
    }
}
