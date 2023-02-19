package ro.editii.scriptorium.tei;

import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.model.TeiFile;

@Deprecated
// abstraction for the cli so that it does not have to start the spring boot for tests
public interface Storage {

    boolean hasAuthor(String strId);

    // these throw exception if argument already saved
    TeiFile saveTeiFile(TeiFile teiFile);
    TeiDiv saveTeiDiv(TeiDiv teiDiv);
    Author saveAuthor(Author author);

    // these return database object if already present
    TeiFile saveOrRetrieveTeiFile(TeiFile teiFile);
    TeiDiv saveOrRetrieveTeiDiv(TeiDiv teiDiv);
    Author saveOrRetrieveAuthor(Author author);

}



