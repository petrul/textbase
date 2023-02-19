package ro.editii.scriptorium.tei


import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import ro.editii.scriptorium.TestConfig
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository
import ro.editii.scriptorium.model.TeiDiv
import ro.editii.scriptorium.model.TeiFile
import ro.editii.scriptorium.service.TeiFileDbService

import javax.sql.DataSource
import javax.transaction.Transactional

@TestPropertySource(properties=[
        "spring.datasource.url=jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto = update"])
@SpringBootTest(classes = [ TestConfig.class ])
class ParseTeiFileIntoDbTest {

    @Autowired
    AuthorRepository authorRepository

    @Autowired
    TeiFileRepository teiFileRepository

    @Autowired
    TeiDivRepository teiDivRepository

    @Autowired
    AuthorStrIdComputer authorStrIdComputer

    @Autowired
    TeiFileDbService teiRepoService

    @Autowired
    TeiRepo teiRepo

    @Autowired
    DataSource dataSource

    @Test
    @Transactional
    void testParserForAlecsandriScrieri() {
        println "jdbc url: " + this.dataSource.connection.metaData.URL;

        def alecsandri_xml = "testrepo/ro/Alecsandri-Scrieri.xml"

        println "*" * 80
        this.teiRepoService.deleteTeiFile(alecsandri_xml)

        assert ! this.teiFileRepository.getByFilename(alecsandri_xml).isPresent()

        def parser = new ParseTeiFileIntoDb(
                alecsandri_xml,
                this.class.classLoader.getResourceAsStream(alecsandri_xml),
                this.class.classLoader.getResource(alecsandri_xml).toURI().toURL(),
                authorRepository,
                teiFileRepository,
                teiDivRepository,
                authorStrIdComputer)

        parser.parse()

        assert this.teiFileRepository.getByFilename(alecsandri_xml).isPresent()

        TeiFile teifile = this.teiFileRepository.getByFilename(alecsandri_xml).get()
        List<TeiDiv> divs = this.teiDivRepository.findByTeiFile(teifile)

        divs.each { assert  ! it.head.trim().isEmpty() }

        println divs.collect { it.head }.join("\n")

        assert divs.findAll { it.head == 'Manifeste și amintiri politice' }.size() == 1

        TeiDiv despot_voda = divs.find { it.urlFragment == "despot_voda" }

        assert despot_voda != null

        println despot_voda

        List subdivurileLuiDespot = despot_voda.getChildren()
        println subdivurileLuiDespot.collect{ it.head }

        // assert PERSONAJELE is directly under Despot Voda, not under the empty div it is really in the xml
        assert subdivurileLuiDespot.find { it.head.trim().equals("PERSONAJELE")} != null

    }


    @Test
    void testServiceImport() {
        final listing = this.teiRepo.list()
//        println listing
        def alecs_tei = listing.findAll { it.contains( 'Alecsandri-Scrieri.xml')}.first()

        println "*" * 80
        this.teiRepoService.deleteTeiFile(alecs_tei)
        println "*" * 80
        this.teiRepoService.importTeiFile(alecs_tei, true)

        this.teiRepoService.deleteTeiFile(alecs_tei)
        println "*" * 80
        this.teiRepoService.importTeiFile(alecs_tei, true)
    }
}
