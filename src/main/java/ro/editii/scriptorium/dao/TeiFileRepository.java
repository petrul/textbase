package ro.editii.scriptorium.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.model.TeiFile;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeiFileRepository extends JpaRepository<TeiFile, Long> {
    List<TeiFile> findByFilename(String filename);

    Optional<TeiFile> getByFilename(String filename);

    @Query("select tf from TeiFile tf join tf.authors a " +
            " where a.strId = ?1")
    List<TeiFile> getTeiFilesForAuthorStrId(String strid);
}
