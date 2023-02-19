package ro.editii.scriptorium.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ro.editii.scriptorium.Globals;
import ro.editii.scriptorium.dao.TeiFileRepository;
import ro.editii.scriptorium.model.TeiFile;
import ro.editii.scriptorium.tei.TeiFileAlreadyImportedException;
import ro.editii.scriptorium.tei.TeiRepo;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    TeiRepo teiRepo;

    @Autowired
    TeiFileRepository teiFileRepository;

    @Autowired
    TeiFileDbService teiFileDbService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void reimportFresherTeis(Writer logActivity) {
        synchronized (Globals.IMPORT_TEIS_WORKING) {
            List<String> filenames = teiRepo.list();

            for (String filename : filenames) {
                File file = teiRepo.getFile(filename);
                Optional<TeiFile> optionalTeiFile = this.teiFileRepository.getByFilename(filename);
                if (optionalTeiFile.isPresent()
                        && optionalTeiFile.get().getTimestamp().getTime() > file.lastModified()) {
                    // do nothing if already imported and file is not fresher than import
                    continue;
                } else {
                    if (optionalTeiFile.isPresent()) {
                        LOG.info("will delete existing import for {} ", filename);
                        writeLn(logActivity, "will delete existing import for " + filename);
                        TeiFile teiFile = optionalTeiFile.get();
                        this.teiFileDbService.deleteTeiFile(filename);
                    }

                    try {
                        LOG.info("will import {} ", filename);
                        writeLn(logActivity, "will delete existing import for " + filename);
                        this.teiFileDbService.importTeiFile(filename, true);
                    } catch (TeiFileAlreadyImportedException e) {
                        LOG.error(e.getMessage(), e);
                    } catch (RuntimeException e) {
                        LOG.error("caught runtime exception logging but will continue with other files", e);
                    }
                }
            }
        }
    }

    public void reimportFile(String filename, Writer logActivity) {
        synchronized (Globals.IMPORT_TEIS_WORKING) {
            writeLn(logActivity,String.format("will now import %s ...", filename));
            File file = teiRepo.getFile(filename);
            Optional<TeiFile> optionalTeiFile = this.teiFileRepository.getByFilename(filename);

            if (optionalTeiFile.isPresent()) {
                LOG.info("will delete existing import for {} ", filename);
                writeLn(logActivity, "will delete existing import for " + filename);
                this.teiFileDbService.deleteTeiFile(filename);
            }

            try {
                LOG.info("will import {} ", filename);
                writeLn(logActivity, "will import " + filename);
                this.teiFileDbService.importTeiFile(filename, true);
            } catch (TeiFileAlreadyImportedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public void reimportAllTeis(Writer logActivity) {
        synchronized (Globals.IMPORT_TEIS_WORKING) {
            writeLn(logActivity, "will now import ...");
            List<String> filenames = teiRepo.list();

            for (String filename : filenames) {
                File file = teiRepo.getFile(filename);
                Optional<TeiFile> optionalTeiFile = this.teiFileRepository.getByFilename(filename);
                if (optionalTeiFile.isPresent()
                        && optionalTeiFile.get().getTimestamp().getTime() > file.lastModified()) {
                    // do nothing if already imported and file is not fresher than import
                    continue;
                } else {
                    if (optionalTeiFile.isPresent()) {
                        LOG.info("will delete existing import for {} ", filename);
                        writeLn(logActivity, "will delete existing import for " + filename);
                        TeiFile teiFile = optionalTeiFile.get();
                        this.teiFileDbService.deleteTeiFile(filename);
                    }

                    try {
                        LOG.info("will import {} ", filename);
                        writeLn(logActivity, "will delete existing import for " + filename);
                        this.teiFileDbService.importTeiFile(filename, true);
                    } catch (TeiFileAlreadyImportedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void destroyAllExistingAndReimportAllTeis(Writer logActivity, boolean iUnderstandThatThisIsAPotentiallyDangerousOperation) {
        synchronized (Globals.IMPORT_TEIS_WORKING) {
            writeLn(logActivity, "will first destroy existing data...");

            this.jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 0");
            this.jdbcTemplate.update("truncate table tei_file_authors");
            this.jdbcTemplate.update("truncate table author");
            this.jdbcTemplate.update("truncate table tei_div");
            this.jdbcTemplate.update("truncate table tei_file");
            this.jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 1");

            List<TeiFile> allExistingTeiFiles = this.teiFileRepository.findAll();
            writeLn(logActivity, "will first destroy existing...");
            for (TeiFile tf : allExistingTeiFiles) {
                writeLn(logActivity, "will destroy " + tf);
                this.teiFileDbService.deleteTeiFile(tf);
            }

            this.reimportAllTeis(logActivity);
        }

    }

    private void writeLn(Writer writer, String s) {
        try {
            writer.write(s + " <br/>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    final private static Logger LOG = LoggerFactory.getLogger(AdminService.class);
}
