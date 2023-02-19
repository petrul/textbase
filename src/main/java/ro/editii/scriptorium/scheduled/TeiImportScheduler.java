package ro.editii.scriptorium.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.editii.scriptorium.Globals;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.dao.TeiFileRepository;
import ro.editii.scriptorium.service.AdminService;
import ro.editii.scriptorium.service.TeiFileDbService;
import ro.editii.scriptorium.tei.AuthorStrIdComputer;
import ro.editii.scriptorium.tei.TeiRepo;

@Component
@Profile("autoimport")
public class TeiImportScheduler {

    @Autowired
    protected TeiFileRepository teiFileRepository;

    @Autowired
    protected AuthorRepository authorRepository;

    @Autowired
    protected TeiDivRepository teiDivRepository;

    @Autowired
    protected AuthorStrIdComputer authorStrIdComputer;

    @Autowired
    protected TeiFileDbService teiFileDbService;

    @Autowired
    TeiRepo teiRepo;

    @Autowired
    AdminService adminService;

    @Scheduled(fixedRate = 15 * 1000)
    public void importTeis() {
        synchronized (Globals.IMPORT_TEIS_WORKING) {
            adminService.reimportFresherTeis(new NoWriter());
        }
    }

    final private static Logger LOG = LoggerFactory.getLogger(TeiImportScheduler.class);
}

