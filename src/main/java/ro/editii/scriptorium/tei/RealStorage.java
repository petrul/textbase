package ro.editii.scriptorium.tei;

import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.dao.TeiFileRepository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.model.TeiFile;

import java.util.List;

@Deprecated
public class RealStorage implements Storage {

    TeiFileRepository teiFileRepository;
    TeiDivRepository teiDivRepository;
    AuthorRepository authorRepository;

    public RealStorage(TeiFileRepository teiFileRepository, TeiDivRepository teiDivRepository, AuthorRepository authorRepository) {
        this.teiDivRepository = teiDivRepository;
        this.teiFileRepository = teiFileRepository;
        this.authorRepository = authorRepository;
    }


    @Override
    public TeiFile saveTeiFile(TeiFile teiFile) {
        return this.teiFileRepository.save(teiFile);
    }

    @Override
    public TeiDiv saveTeiDiv(TeiDiv teiDiv) {
        return this.teiDivRepository.save(teiDiv);
    }

    @Override
    public Author saveAuthor(Author author) {
        return this.authorRepository.save(author);
    }

    @Override
    public TeiFile saveOrRetrieveTeiFile(TeiFile teiFile) {
        List<TeiFile> teiFilesFromRepo = this.teiFileRepository.findByFilename(teiFile.getFilename());
        if (teiFilesFromRepo.size() > 0) {
            assert teiFilesFromRepo.size() == 1;
            teiFile = teiFilesFromRepo.iterator().next();
        } else {
            teiFile = this.saveTeiFile(teiFile);
        }

        return teiFile;
    }

    @Override
    public TeiDiv saveOrRetrieveTeiDiv(TeiDiv teiDiv) {
        TeiDiv div = teiDiv;
        List<TeiDiv> listOfDivs = this.teiDivRepository.findByTeiFileAndXpath(teiDiv.getTeiFile(), teiDiv.getXpath());
        if (listOfDivs.size() > 0) {
            assert listOfDivs.size() == 1;
            div = listOfDivs.iterator().next();
        } else
            div = this.saveTeiDiv(div);

        return div;
    }

    @Override
    public Author saveOrRetrieveAuthor(Author author) {
        List<Author> listOfAuthors = this.authorRepository.findByOriginalNameInTeiFile(author.getOriginalNameInTeiFile());
        if (listOfAuthors.size() > 0) {
            assert listOfAuthors.size()  == 1;
            author = listOfAuthors.iterator().next();
        } else {
            author = this.saveAuthor(author);
        }

        return author;

    }

    @Override
    public boolean hasAuthor(String strId) {
        List<Author> byStrId = this.authorRepository.findByStrId(strId);
        if (byStrId != null && byStrId.size() == 1 && byStrId.iterator().next().equals(strId))
            return true;
        else
            return false;
    }
}
