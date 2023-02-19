package ro.editii.scriptorium.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository
import ro.editii.scriptorium.service.AdminService
import ro.editii.scriptorium.tei.TeiRepo

@Configuration
class GroovyShellConfig {

    @Autowired
    TeiRepo teiRepo;

    @Autowired
    TeiDivRepository teiDivRepository

    @Autowired
    TeiFileRepository teiFileRepository

    @Autowired
    AuthorRepository authorRepository

    @Autowired
    AdminService adminService

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    GroovyShell groovyShell() {
        Binding binding = new Binding(
                'ctxt': applicationContext,
                'tei_repo': teiRepo,
                'div_repo': teiDivRepository,
                'file_repo': teiFileRepository,
                'auth_repo': authorRepository,
                'admin': adminService,
        )

        return new GroovyShell(binding)
    }

}

@Controller
class GroovyShellRestController {

    @Autowired
    GroovyShell groovyShell;

    GroovyShellRestController() {
    }

    @PostMapping(value = "/api/shell", consumes = ["text/plain"])
    @ResponseBody
    Object execute(@RequestBody final String script) {

        LOG.debug(String.format("will execute script [%s]", script))

        Object res = this.groovyShell.parse(script).run()

        LOG.debug(String.format("returning response of class %s : [%s]", res.getClass().getCanonicalName(), res.toString()))

        return res
    }

    static Logger LOG = LoggerFactory.getLogger(GroovyShellRestController.class)
}
