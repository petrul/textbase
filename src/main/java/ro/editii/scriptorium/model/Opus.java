package ro.editii.scriptorium.model;

import javax.persistence.*;
import java.util.List;

//@Entity
@Deprecated
public class Opus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    TeiDiv teiDiv; // the teiDiv that contains this work

    String title;

    @ManyToMany
    List<Author> authors;

    String language;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public Author getFirstAuthor() {
        return this.authors.iterator().next();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
