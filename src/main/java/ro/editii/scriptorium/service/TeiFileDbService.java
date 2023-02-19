package ro.editii.scriptorium.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.tei.TeiRepo;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.dao.TeiFileRepository;
import ro.editii.scriptorium.model.TeiFile;
import ro.editii.scriptorium.tei.AuthorStrIdComputer;
import ro.editii.scriptorium.tei.ParseTeiFileIntoDb;
import ro.editii.scriptorium.tei.TeiFileAlreadyImportedException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

/**
 * this is a service over {@link TeiFileRepository}, so over the database not over the {@link TeiRepo}
 */
@Service
public class TeiFileDbService {

    @Autowired
    TeiFileRepository teiFileRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    TeiDivRepository teiDivRepository;

    @Autowired
    TeiRepo teiRepo;

    @Autowired
    AuthorStrIdComputer authorStrIdComputer;

    ParseTeiFileIntoDb newParser(String filename, InputStream is) {
        File file = this.teiRepo.getFile(filename);
        try {
            return new ParseTeiFileIntoDb(filename, is, file.toURI().toURL(),
                    authorRepository, teiFileRepository, teiDivRepository, authorStrIdComputer);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteTeiFile(String teiFilename) {
            Optional<TeiFile> optionalTeiFile = this.teiFileRepository.getByFilename(teiFilename);

            if (optionalTeiFile.isPresent())
                this.deleteTeiFile(optionalTeiFile.get());
    }

    protected void deleteTeiFile(TeiFile dbTeiFile) {
        List<TeiDiv> opuses = this.teiDivRepository.getOperaForTeiFileId(dbTeiFile.getId());

        for (TeiDiv op: opuses) {
            this.delete_rec(op);
        }
        this.teiFileRepository.delete(dbTeiFile);

        List<Author> authors = dbTeiFile.getAuthors();

        for (Author author : authors) {
            // if author has no attached teifiles, delete the author too
            List<TeiFile> teiFiles = this.authorRepository.getTeiFiles(author.getId());

            if (teiFiles.size() == 0)
                this.authorRepository.deleteById(author.getId());
        }
    }

    protected void delete_rec(TeiDiv div) {
        List<TeiDiv> children = div.getChildren();

        for (TeiDiv child: children)
            this.delete_rec(child);

        this.teiDivRepository.deleteById(div.getId());
    }

    @Transactional
    public void importTeiFile(String teiFilename, boolean forceReimport) throws TeiFileAlreadyImportedException {
        InputStream streamForFile = this.teiRepo.getStreamForName(teiFilename);
        ParseTeiFileIntoDb parcurgere = this.newParser(teiFilename, streamForFile);

        Optional<TeiFile> optionalTeiFile = this.teiFileRepository.getByFilename(teiFilename);
        if (optionalTeiFile.isPresent()) {
            if (forceReimport)
                this.deleteTeiFile(teiFilename);
            else
                return;
        }

        parcurgere.parse();
    }
}
