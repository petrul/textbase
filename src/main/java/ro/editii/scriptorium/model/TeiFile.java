package ro.editii.scriptorium.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * represent a TEI xml file
 */
@Entity
@Data
public class TeiFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String filename;

    @Size(max=1000)
    String title;

    @ManyToMany
    List<Author> authors;

    Timestamp timestamp = new Timestamp(new Date().getTime());


    public Author getAuthor() {
        if (this.authors.size() != 1)
            throw new IllegalStateException(String.format("expected one author, have %d instead", this.authors.size()));
        return this.authors.iterator().next();
    }
    
}
