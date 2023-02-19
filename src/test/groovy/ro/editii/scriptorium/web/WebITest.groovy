package ro.editii.scriptorium.web

import editii.commons.xml.XpathTool
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.transaction.AfterTransaction
import org.springframework.test.context.transaction.BeforeTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ro.editii.scriptorium.GTestUtil
import ro.editii.scriptorium.TestConfig
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.RelocationRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository
import ro.editii.scriptorium.model.Relocation
import ro.editii.scriptorium.service.AdminService
import ro.editii.scriptorium.service.DivService
import ro.editii.scriptorium.service.TeiFileDbService
import ro.editii.scriptorium.tei.AuthorStrIdComputer
import ro.editii.scriptorium.tei.ParseTeiFileIntoDb
import ro.editii.scriptorium.tei.TeiRepo

import javax.imageio.ImageIO
import javax.persistence.EntityManager
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@TestPropertySource(properties=[
        "spring.datasource.url=jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = [ TestConfig.class ])
class WebITest {

    @LocalServerPort
    private int port

    @Autowired DivController divController
    @Autowired private TestRestTemplate restTemplate
    @Autowired private RestTemplate restTemplateNoRedirect
    @Autowired TeiRepo teiRepo
    @Autowired TeiFileDbService teiRepoService
    @Autowired TeiFileRepository teiFileRepository
    @Autowired TeiDivRepository teiDivRepository
    @Autowired AuthorRepository authorRepository
    @Autowired AdminService adminService
    @Autowired RelocationRepository relocationRepository
    @Autowired AuthorStrIdComputer authorStrIdComputer
    @Autowired DivService divService
    @Autowired PlatformTransactionManager platformTransactionManager

    def url2file = [
            "/creanga/povesti"                  : "",
            "/alecsandri/legende/dumbrava_rosie": ""
    ]

    def urls = [
            "/creanga/povesti"                  : "Iubite cetitoriu,",
            "/alecsandri/legende/dumbrava_rosie": "dedicat amicului meu C. Negri",
    ]

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


    @Test @Transactional(propagation = Propagation.REQUIRED)
    void getCreangaPovesti() {
        assert divController != null;

        urls.each { k, v ->
            def url = "http://localhost:" + port + k;
            def expected_text = v;
            assert this.restTemplate.getForObject(url, String.class)
                    .contains(expected_text)
        }
    }

    /**
     * a toc page (like /creanga/povesti) should only contain links to chapters, not really the text
     */
    @Test @Transactional
    void aTocPageDoesNotContainActualText() {

        assert countTableRows("author") > 0
        assert countTableRows("tei_file_authors") > 0
        assert countTableRows("tei_div") > 0
        println this.jdbcTemplate.queryForList("select * from author");

        File f = teiRepo.getFile("/ro/Creanga-Amintiri_din_copilarie.xml")
        assert f.exists() && f.canRead()

        def xt = new XpathTool(new FileInputStream(f), f.getAbsolutePath())
        final xpath_for_povesti = "/tei:TEI/tei:text/tei:body/tei:div[2]"
        final xpath_for_soacra_cu_3_nurori = "/tei:TEI/tei:text/tei:body/tei:div[2]/tei:div[1]"
        assert xpath_for_povesti != xpath_for_soacra_cu_3_nurori
        assert xpath_for_soacra_cu_3_nurori.startsWith(xpath_for_povesti)
        assert xpath_for_soacra_cu_3_nurori.length() > xpath_for_povesti.length()

        assert xt.applyXpathForNodeSet(xpath_for_povesti).length == 1
        assert xt.applyXpathForNodeSet(xpath_for_soacra_cu_3_nurori).length == 1
        assert xt.applyXpathForNodeSet(xpath_for_soacra_cu_3_nurori + "/ttt").length == 0

        def url = "http://localhost:" + port + "/creanga/povesti"
        String text = this.restTemplate.getForObject(url, String.class)

        assert text.contains("Iubite cetitoriu") // the motto should appear
        assert text.contains("Soacra cu trei nurori") // the motto should appear
        assert !text.contains("Era odată o babă, care avea trei feciori înalți ca niște brazi și tari de virtute, dar slabi de minte.")
        // but not the content of one of the stories


        // but the actual sub-story contains the text
        def soacra_url = "http://localhost:" + port + "/creanga/povesti/soacra_cu_trei_nurori"
        String text2 = this.restTemplate.getForObject(soacra_url, String.class)

        assert text2.length() > 0
        assert text2.contains("Era odată o babă, care avea trei feciori înalți ca niște brazi și tari de virtute, dar slabi de minte.")

    }

    def p(args) {
        println(args)
    }

    @Test @Transactional
    public void alecsandri_suvenire_maiorului_iancu_bran() {
        def url = "http://localhost:${port}/alecsandri/suvenire/maiorului_iancu_bran"
        def text = this.restTemplate.getForObject(url, String.class)
        println text
        assert text.contains("<h3>Maiorului Iancu Bran")
        assert text.contains("Mergi să-ți iei dreapta răsplată de la dreptul ziditor")
        assert text.contains("Tu ce lași în urmă jale, vrednicule muritor!")

        assert text.length() > 0
    }

    @Test @Transactional
    public void cantemirGetBinaryJpeg() {
        // you can get the _binary at any level, as long as you have the id
        final urls = [
                "http://localhost:${port}/cantemir/descrierea_moldovei/_binary/d3e5875",
                "http://localhost:${port}/cantemir/descrierea_moldovei/partea_eclesiastica_si_literara/_binary/d3e5875",
                "http://localhost:${port}/cantemir/descrierea_moldovei/partea_eclesiastica_si_literara/despre_literile_moldovenilor/_binary/d3e5875"
        ]
        for (String url : urls) {
            final resp = this.restTemplate.getForEntity(url, byte[].class)

            p resp.headers
            resp.headers.each {
                println it.class
                println it.key
                println it.key.class
                println it.value
                p it.value.class
            }
            final contentTypeHeader = resp.headers
                    .findAll {it.key == 'Content-Type' && it.value == ['image/jpeg']};
            assert contentTypeHeader.size() == 1
            final bytes = resp.body
            assert bytes != null
            assert bytes.length > 0

            final image = ImageIO.read(new ByteArrayInputStream(bytes))

            assert image != null
            assert image.width == 1166
            assert image.height == 1220

        }
    }

    @Test @Transactional
    public void testCantemirDescrierea_page_actually_contains_correct_image_link() {
        final pageUrl = "http://localhost:${port}/cantemir/descrierea_moldovei/partea_eclesiastica_si_literara/despre_literile_moldovenilor"
        final content = this.restTemplate.getForEntity(pageUrl, String.class).body
        assert content.contains('src="/cantemir/descrierea_moldovei/partea_eclesiastica_si_literara/despre_literile_moldovenilor/_binary/d3e5875"')

        // assert page's twitter and facebook card images point to that image
        def lines = content.split("\n").findAll(it -> it.contains('meta property="og:image"'))
        assert lines != null
        assert lines.size() == 1

        final imageUrl = "http://localhost:${port}/cantemir/descrierea_moldovei/_binary/d3e5875"
        assert lines.first().contains(imageUrl)
        assert content.split("\n").findAll(it -> it.contains('meta name="twitter:image"')).first().contains(imageUrl)


    }

    @Test @Transactional
    void testRandomTool() {
        final url = "http://localhost:${port}/util/random"
        final resp = this.restTemplateNoRedirect
                .getForEntity(url, String.class)
        assert resp.statusCodeValue == HttpStatus.FOUND.value()
        assert resp.headers.get("Location") != null
        assert resp.headers.get("Location").size() == 1
        assert resp.headers.get("Location")[0].length() > 1
        assert resp.headers.get("Location")[0].startsWith("http://localhost:${port}/")
    }

    @Test
    void relocationWorks() {
        final authorId = 'alecsandri'
        final opId = 'poezii'

        final tt = new TransactionTemplate(this.platformTransactionManager)
        def executor = Executors.newSingleThreadExecutor()

        assert this.authorRepository.getByStrId(authorId).empty
        assert JdbcTestUtils.countRowsInTable(this.jdbcTemplate, 'tei_div') == 0
        assert JdbcTestUtils.countRowsInTable(this.jdbcTemplate, 'author') == 0

        final tei = GTestUtil.teiOf("Văsălie Alecsandri", """
            <div>
                <head>Poezii</head>
                <p>content</p>
            </div>
        """)
        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)
        parser.parse()

        // make sure data commited on test main thread is visible from another thread
        final mainThreadName =  Thread.currentThread().name
        assert this.authorRepository.getByStrId(authorId).present
        def fut = executor.submit(() -> {
            assert mainThreadName != Thread.currentThread().name
            assert this.authorRepository.getByStrId(authorId).present
        })
        fut.get(1, TimeUnit.MINUTES)

        assert this.divService.getOpera(authorId).collect{it.urlFragment}.contains(opId)
        assert this.teiDivRepository
                .findOperaForAuthorStrId(authorId)
                .collect{it.urlFragment}
                .contains(opId)

        final String nonExistentPath = "/$authorId/$opId/" + RandomStringUtils.randomAlphanumeric(200)
        final String relocationPath = RandomStringUtils.randomAlphanumeric(200);

        // this is the author page, it should exist
        assert this.restTemplateNoRedirect.getForEntity(url("/$authorId/"), String.class).body.containsIgnoreCase(authorId)

        // assert not teidiv points to that randomly generated 200-char string
        assert JdbcTestUtils.countRowsInTable(jdbcTemplate, 'relocation') == 0
        tt.execute( status -> {
            assert this.teiDivRepository.findAll().stream()
                    .noneMatch(it -> it.getCompletePath() == nonExistentPath)
        })

        // getting it by url should fail

        final nonExistentUrl = url(nonExistentPath)
        p nonExistentUrl

        try {
            this.restTemplateNoRedirect.getForEntity(nonExistentUrl, String.class)
            Assertions.fail("call should fail")
        } catch ( HttpClientErrorException e) {
            assert e.statusCode == HttpStatus.NOT_FOUND
            assert e.responseHeaders.get(HttpHeaders.LOCATION) == null
        }

        // insert relocation into db
        assert this.relocationRepository.findById(nonExistentPath).empty

        tt.execute((status) -> {
            this.relocationRepository.save(Relocation.builder()
                    .oldPath(nonExistentPath)
                    .newPath(relocationPath)
                    .build())
            status.flush()
        })

        assert this.relocationRepository.findAll().size() > 0

        // assert that db change is visible in other threads
        Future<List<Relocation>> res = executor.submit(() -> {
                p "=> thread " + Thread.currentThread().name
                return tt.execute((TransactionStatus status) -> this.relocationRepository.findAll() as TransactionCallback<List<Relocation>>)
            } as Callable<List<Relocation>>)
        p res.get(2, TimeUnit.DAYS)
        p res.get().class
        assert res.get().size() > 0

        assert this.relocationRepository.findById(nonExistentPath).present
        def byId = this.relocationRepository.findById(nonExistentPath).get()
        assert byId.oldPath == nonExistentPath
        assert byId.newPath == relocationPath

        // re-get by url, this time no 404 should occur but rather 301 Re-Location:
        def resp = this.restTemplateNoRedirect.getForEntity(nonExistentUrl, String.class)
        assert resp.statusCode == HttpStatus.MOVED_PERMANENTLY
        final locationHeaders = resp.headers[HttpHeaders.LOCATION]
        assert locationHeaders.size() == 1
        assert  locationHeaders.first() == url(relocationPath)
    }

    def url(String path) {
        return "http://localhost:${this.port}/$path"
    }
}
