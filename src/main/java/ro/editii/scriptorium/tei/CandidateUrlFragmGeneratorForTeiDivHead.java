package ro.editii.scriptorium.tei;


import org.apache.logging.log4j.util.Strings;
import ro.editii.scriptorium.Util;
import ro.editii.scriptorium.model.TeiDiv;

import java.util.*;
import java.util.stream.Collectors;

/**
 * use this as an infinite stream of propositions of urlFragm for an TeiDiv
 */
public class CandidateUrlFragmGeneratorForTeiDivHead implements Iterable<String> {

    String head;

    public CandidateUrlFragmGeneratorForTeiDivHead(String head) {
        this.head = head;
    }

    @Override
    public Iterator<String> iterator() {
        return new CandidateUrlFragmGeneratorForTeiDivHead_Iterator(this.head);
    }
}

class CandidateUrlFragmGeneratorForTeiDivHead_Iterator implements Iterator<String> {

    public static String[] PREPOZITII_SI_CONJUNCTII = {
            "al", "ale", "and", "a",
            "à", "au", "aux",
            "care", "cand", "când", "ce", "ci", "ca", "cel", "cum", "cu",
            "comme", "ceux",
            "da", "de", "din", "dar", "despre",
            "du", "dans", "des",
            "et", "en",
            "for", "from",
            "iar", "însă", "insa", "in", "il", "intr",
            "lui", "le", "la", "les", "laquelle",
            "mai",
            "n", "nu", "nici",
            "o", "ori", "on", "or", "of", "ou", "out",
            "pe", "peste", "prea", "prin", "pentru", "pro",
            "pour", "par",
            "que", "qui", "quel", "quelle",
            "sau", "sa", "si", "sub",
            "some",
            "the"
    };

    // add english, german, french, spanish, russian, chinese

    List<String> firstPropositions = new ArrayList<>();
    Iterator<String> firstPropositionsIt;

    String[] splits;
    int splits_index = 0;
    String lastAddedSplit;


    int crtEndIncrement = 2; // increment starts from 2

    // last word-composed proposition, which will be used as base for ever-increasing candidates
    String growingProposition = null;
    String lastCandidate = null;


    static Set<String> PREP_AND_CONJ_AS_LIST = new HashSet<>(Arrays.asList(PREPOZITII_SI_CONJUNCTII));

    CandidateUrlFragmGeneratorForTeiDivHead_Iterator(String head) {
        String urlFriendify = Util.urlFriendify(head);

        this.splits = urlFriendify.split("_");
        final List<String> significantSplits = Arrays.stream(this.splits)
                .filter(it -> it.length() > 2) // except one or two-lettered words
                .filter(it -> !PREP_AND_CONJ_AS_LIST.contains(it))
                .collect(Collectors.toList());

        if (Strings.isEmpty(urlFriendify)) {
            this.firstPropositions.add("1");
        } else if (significantSplits.size() < 4) {
            this.firstPropositions.add(urlFriendify);
        } else {
            // actually for opus urlfragm, this should be three (i.e. romanii_supt_mihai); chapters should be two.
            // String first_three_words = Arrays.asList(splits).subList(0, 2).stream().collect(Collectors.joining("_"));
            // this.firstPropositions.add(first_three_words);
        }
        this.firstPropositionsIt = this.firstPropositions.iterator();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    Set<String> previousCandidates = new HashSet<>();

    @Override
    public String next() {

        StringBuffer buffer = new StringBuffer();
        if (this.growingProposition != null)
            buffer = new StringBuffer(this.growingProposition);

        // initial propositions built in the constructor are not done, serve them
        if (this.firstPropositionsIt.hasNext()) {
            final String crt = this.firstPropositionsIt.next();
            this.previousCandidates.add(crt);
            this.lastCandidate = crt;
            return crt;
        }

        // do while the new proposed candidate has already been proposed
        // (rare but possible when the first proposal comes from the constructor and then the iterator re-builds it)
        do {
            // if last proposition was a number, ever increase it.
            try {
                int asNumber = Integer.parseInt(this.lastCandidate);
                buffer = new StringBuffer(Integer.valueOf(asNumber + 1).toString());
                final String crt = buffer.toString();
                this.lastCandidate = crt;
                return crt;
            } catch (NumberFormatException e) {
                // last proposition was not a number, continue algorithm
            }

            if (this.splits_index == 0)
                this.growingProposition = null;

            boolean isAuxiliary;
            if (this.splits_index < this.splits.length) {

                // do while we only meet auxiliaries (add all auxiliaries in a bunch)
                do {
                    if (buffer.length() > 0)
                        buffer.append('_');

                    final String crt = splits[splits_index++];
                    isAuxiliary = this.isAuxiliary(crt);
                    buffer.append(crt);
                    this.lastAddedSplit = crt;

                } while (isAuxiliary && splits_index < splits.length);

                this.growingProposition = buffer.toString();
            } else {
                // the head splits are terminated, the only way to generate new is to add _1, _2, _3 etc at the end
                buffer = new StringBuffer(this.growingProposition);
                buffer.append("_" + this.crtEndIncrement++);
            }

        } while (this.previousCandidates.contains(buffer.toString()));

        String crtProposition = buffer.toString();
        this.previousCandidates.add(crtProposition);
        this.lastCandidate = crtProposition;
        if (crtProposition.length() > TeiDiv.MAX_URL_FRAGM_SIZE)
            crtProposition = crtProposition.substring(0, TeiDiv.MAX_URL_FRAGM_SIZE);

        return crtProposition;
    }

    /**
     * auxiliary means conjuction, preposition or number
     */
    protected boolean isAuxiliary(String fragment) {
        return PREP_AND_CONJ_AS_LIST.contains(fragment)
                || fragment.matches("^\\d+$");
    }

}