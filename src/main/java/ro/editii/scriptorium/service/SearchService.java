package ro.editii.scriptorium.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    TeiDivRepository teiDivRepository;

    public AuthorsAndTeiDivs searchAuthorsAndWorks(String q) {
        if (q.length() >= 3) {
            List<Author> lastnames  = this.authorRepository.findByLastNameContainingIgnoreCase(q);
            List<Author> firstnames = this.authorRepository.findByFirstNameContainingIgnoreCase(q);

            List<Author> authors = new ArrayList<>();
            authors.addAll(lastnames);
            authors.addAll(firstnames);

            List<TeiDiv> divs = this.teiDivRepository.findByHeadContainingIgnoreCase(q);
            return AuthorsAndTeiDivs.builder()
                    .authors(authors)
                    .teiDivs(divs).build();
        } else
            throw new IllegalArgumentException("query string must have more than 3 letters");
    }
}
