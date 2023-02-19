package ro.editii.scriptorium.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiFile;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByStrId(String strId);
    List<Author> findByOriginalNameInTeiFile(String originalName);

    Optional<Author> getByStrId(String strId);
    Optional<Author> getByOriginalNameInTeiFile(String originalName);

    @Query("select tf from TeiFile tf join tf.authors a where a.id = ?1")
    List<TeiFile> getTeiFiles(long authorId);

    List<Author> findByLastNameContainingIgnoreCase(String excerpt);
    List<Author> findByLastNameIgnoreCase(String excerpt);
    List<Author> findByFirstNameContainingIgnoreCase(String excerpt);

}
