package ro.editii.scriptorium.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthorsAndTeiDivs {
    List<Author> authors;
    List<TeiDiv> teiDivs;
}
