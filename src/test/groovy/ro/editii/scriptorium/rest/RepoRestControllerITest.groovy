package ro.editii.scriptorium.rest;

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.AfterTransaction
import org.springframework.test.context.transaction.BeforeTransaction
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import ro.editii.scriptorium.TestConfig
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dto.AuthorDto
import ro.editii.scriptorium.service.AdminService
import ro.editii.scriptorium.service.TeiFileDbService
import ro.editii.scriptorium.tei.TeiRepo
import ro.editii.scriptorium.web.DivController

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = [ TestConfig.class ])
class RepoRestControllerITest {

    @LocalServerPort
    private int port

    @Autowired DivController divController
    @Autowired private TestRestTemplate restTemplate
    @Autowired private RestTemplate restTemplateNoRedirect
    @Autowired AuthorRepository authorRepository
    @Autowired TeiDivRepository teiDivRepository
    @Autowired TeiRepo teiRepo
    @Autowired TeiFileDbService teiRepoService
    @Autowired AdminService adminService

    @Autowired
    JdbcTemplate jdbcTemplate

    def countTableRows(tableName) {
        return this.jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class)
    }

    @Autowired
    EntityManager entityManager


    void truncateAllTables() {
        this.jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 0")
        this.jdbcTemplate.update("truncate table tei_file_authors")
        this.jdbcTemplate.update("truncate table author");
        this.jdbcTemplate.update("truncate table tei_div");
        this.jdbcTemplate.update("truncate table tei_file");
        this.jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 1")

        assert countTableRows("author") == 0
        assert countTableRows("tei_file_authors") == 0
        assert countTableRows("tei_div") == 0
    }


    @BeforeTransaction
    void beforeAll() {
        this.truncateAllTables()

        adminService.reimportAllTeis(new OutputStreamWriter(System.out))

        assert countTableRows("author") > 0
        assert countTableRows("tei_file_authors") > 0
        assert countTableRows("tei_div") > 0

    }

    @AfterTransaction
    void afterTransaction() {
        this.truncateAllTables();
    }

    @Test @Transactional
    void getAuthors() {
        def url = "http://localhost:" + port + "/api/authors/"
        List<AuthorDto> authors = this.restTemplate.getForEntity(url, List<AuthorDto>.class).body
        assert  authors != null
        assert authors.size() > 0
        p authors
        authors.each { p it }
        final ids = authors.collect { it -> it.strId }
        ['alecsandri', 'creanga', 'cantemir'].each {
            assert ids.contains(it)
        }
    }

    @Test @Transactional
    void getAuthor() {
        final alecsandri = 'alecsandri'
        def url = "http://localhost:" + port + "/api/authors/$alecsandri"
        AuthorDto author = this.restTemplate.getForEntity(url, AuthorDto.class).body
        p author
        assert author != null
        assert author.strId == alecsandri
        assert author.opera != null
        assert author.opera.size() > 0

        p author.opera
        p author.opera.size()
        author.opera.each {
            assert it.head != null ;
            assert it.head.size() > 0
            assert it.urlFragment != null
            assert it.urlFragment.size() > 0
            assert it.path != null
            assert it.path.size() > 0
        }

        final operaHeads = author.opera.collect{ it.head}
        p operaHeads
    }

    def p(args) {
        println(args)
    }
}