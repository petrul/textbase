package ro.editii.scriptorium.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.model.TeiFile;

import java.util.List;

@Repository
public interface TeiDivRepository extends JpaRepository<TeiDiv, Long> {


    List<TeiDiv> findByHead(String head);

    List<TeiDiv> findByTeiFile(TeiFile teiFile);

    List<TeiDiv> findByTeiFileAndXpath(TeiFile teiFile, String xpath);

    /**
     * return "root" divs, corresponding to h1 in office file, should be an opus,aka work name
     */
    @Query("select div from TeiDiv div join div.teiFile tf " +
            " where div.parent is null and tf.id = ?1")
    List<TeiDiv> getOperaForTeiFileId(long teiFileId);

    // aka divs without div children
    @Query("select count(parent) from TeiDiv parent left outer join parent.children c where c is null ")
    int getNrOfBottomDivs();

    @Query("select div from TeiDiv div " +
            " join div.teiFile.authors a " +
            " where div.parent is null " +
            " and a.strId = ?1 ")
    List<TeiDiv> findOperaForAuthorStrId(String authorStrId);

    List<TeiDiv> findByHeadContainingIgnoreCase(String excerpt);

    List<TeiDiv> findByUrlFragmentAndParent(String urlFragment, TeiDiv parent);

    @Query("select d.teiFile.authors from TeiDiv d where d.id = ?1")
    List<Author> getAuthors(long id);

}
