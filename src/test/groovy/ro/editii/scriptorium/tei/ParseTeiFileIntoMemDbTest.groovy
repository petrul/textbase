package ro.editii.scriptorium.tei


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import ro.editii.scriptorium.TestConfig
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository

import static ro.editii.scriptorium.GTestUtil.*

@TestPropertySource(properties=[
        "spring.datasource.url=jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto = create"])
@SpringBootTest(classes = [ TestConfig.class ])
class ParseTeiFileIntoMemDbTest {

    @Autowired
    AuthorRepository authorRepository

    @Autowired
    TeiFileRepository teiFileRepository

    @Autowired
    TeiDivRepository teiDivRepository

    @Autowired
    AuthorStrIdComputer authorStrIdComputer

    @Autowired
    JdbcTemplate jdbcTemplate

    @BeforeEach
    void before() {

        this.jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 0")
        this.jdbcTemplate.update("truncate table tei_file_authors")
        this.jdbcTemplate.update("truncate table author")
        this.jdbcTemplate.update("truncate table tei_div")
        this.jdbcTemplate.update("truncate table tei_file")
        this.jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 1")

        assert countTableRows(this.jdbcTemplate, "author") == 0
        assert countTableRows(this.jdbcTemplate, "tei_file_authors") == 0
        assert countTableRows(this.jdbcTemplate, "tei_div") == 0
    }

    @Test
    void disabledChapters() {
        assert ParseTeiFileIntoDb.isMarkedWithX("[x] foaie verde")
        assert ParseTeiFileIntoDb.isMarkedWithX("[X] foaie verde")
        assert ParseTeiFileIntoDb.isMarkedWithX("[Xx] foaie verde")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie [Xx] verde")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie [x] verde")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  [Xx] ")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  [x] ")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  [x]")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  [X] ")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  /X/ ")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  /x/ ")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  |x| ")
        assert ParseTeiFileIntoDb.isMarkedWithX("foaie verde  |X| ")

        final tei = """
        <TEI xmlns="http://www.tei-c.org/ns/1.0">
           <teiHeader>
              <fileDesc>
                 <titleStmt>
                    <title>???????????????? ??????????????????</title>
                    <author>??????????????????????, ?????????? ????????????????????</author>
                 </titleStmt>
              </fileDesc>
           </teiHeader>
           <text>                    
              <body>
                 <head>file Title</head>
                 <author>??????????????????????, ?????????? ????????????????????</author>
                 <subtitle>1988</subtitle>
                 
                 <div type="div1">
                    <head>This heading will pass OK</head>
                    <subtitle>( ???????????????? ?????????????????? ?? ???????????????????? ?????????? - 1 )</subtitle>
                    <p rend="justify">?????????? ???????????????????? ??????????????????????</p>
                 </div>
                 <div type="div1">
                    <head>This heading will be ignored [x]</head>
                    <subtitle>( ???????????????? ?????????????????? ?? ???????????????????? ?????????? - 1 )</subtitle>
                    <p rend="justify">?????????? ???????????????????? ??????????????????????</p>
                 </div>
                 <div type="div1">
                    <head>This heading will also be ignored [X]</head>
                    <subtitle>( ???????????????? ?????????????????? ?? ???????????????????? ?????????? - 1 )</subtitle>
                    <p rend="justify">?????????? ???????????????????? ??????????????????????</p>
                 </div>
                 <div type="div1">
                    <head>This heading will also be ignored [X]</head>
                 </div>
                 <div type="div1">
                        <head>This heading will also be ignored [Xx]</head>
                        <subtitle>( ???????????????? ?????????????????? ?? ???????????????????? ?????????? - 1 )</subtitle>
                        <p rend="justify">?????????? ???????????????????? ??????????????????????</p>
                 </div>
                 <div type="div1">
                        <head>But this will pass x X]</head>
                        <subtitle>( ???????????????? ?????????????????? ?? ???????????????????? ?????????? - 1 )</subtitle>
                        <p rend="justify">?????????? ???????????????????? ??????????????????????</p>
                 </div>
                 <div type="div1">
                        <head>I d like this to be ignored /x/ too</head>
                        <subtitle>( ???????????????? ?????????????????? ?? ???????????????????? ?????????? - 1 )</subtitle>
                        <p rend="justify">?????????? ???????????????????? ??????????????????????</p>
                 </div>
                 
         </body></text></TEI>
        """

        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)

        parser.parse()
        assert this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class) == 2

        def list = this.jdbcTemplate.queryForList("select head from tei_div;", String.class)
        list.each {p it}
        assert list.find {it.contains('[')} == null
    }

    @Test
    void dumasSkipsABook() {
        final tei = """
        <TEI xmlns="http://www.tei-c.org/ns/1.0">
           <teiHeader>
              <fileDesc>
                 <titleStmt>
                    <title>title</title>
                    <author>Dumas,Alexandre</author>
                 </titleStmt>
              </fileDesc>
           </teiHeader>
           <text>                    
              <body>
                 <head>file Title</head>
                 <author>Dumas,Alexandre</author>
                 <subtitle>1988</subtitle>
                 
         <div type="div1">
            <head>
               <hi rend="bold">La princesse </hi>
               <hi rend="bold">F</hi>
               <hi rend="bold">lora</hi>
            </head>
            <subtitle>(1862)</subtitle>
            <div type="div2" rend="P274">
               <head>
                  <anchor type="bookmark-start" xml:id="id___RefHeading___Toc86913708"/>I<lb/>La princesse Flora ?? sa parente, ?? Moscou.<ptr type="bookmark-end" target="#id___RefHeading___Toc86913708"/>
               </head>
               <p>Je suis furieuse contre Moscou, ma ch??re, parce que tu n'es pas avec moi. Je dois te raconter une foule de choses??? mais comment te les ??crire??? J'ai tant vu et tant v??cu depuis une semaine??! D'abord, j'ai ??t?? mortellement triste??: rien n'est plus ennuyeux qu'un continuel ??tonnement. La cour imp??riale et le grand monde me donnent le vertige, et j'en suis arriv?? ?? entendre sans m'??merveiller la plus ??norme sottise, comme ?? contempler sans sourire le plus curieux tableau??; mais la f??te de Peterhoff, Peterhoff lui-m??me, c'est une exception, la perle des exceptions jusqu'?? pr??sent??? J'ai tout vu??; j'ai ??t?? partout??; j'ai les oreilles assourdies du bruit du canon, des cris du peuple, du murmure des fontaines, du rebondissement des cascades??? Nous avons lu avec attention, nous avons d??vor?? avec gourmandise ensemble, tu te le rappelles, la description des miracles de Peterhoff??; mais, quand j'ai vu de mes propres yeux toutes ces merveilles, elles m'ont litt??ralement d??vor??e, et j'ai tout oubli??, m??me toi, mon bel ange??; j'ai rebondi dans les airs avec la cascade??; j'ai mont?? jusqu'au ciel avec sa poussi??re??; je suis redescendue sur la terre, l??g??re comme la goutte de ros??e??; j'ai jet?? mon ombre c??leste et odorif??rante, sur les all??es pleines de souvenirs??; j'ai jou?? avec les rayons du soleil et avec les vagues de la mer??; et tout cela, c'??tait le jour??; et quelle nuit a couronn?? ce jour??! Il fallait s'??tonner en voyant comme peu ?? peu s'allumait l'illumination??; il semblait qu'un doigt de feu dessin??t de merveilleux dessins sur le voile noir de la nuit??; elle s'??panouissant en fleurs, s'arrondissait en roue, rampait en serpent, et, tout ?? coup, voil?? que tout le jardin fut en feu. Tu eusses dit, ma ch??re, que le soleil ??tait tomb?? du ciel sur la terre et s'y ??tait ??parpill?? en ??tincelles??; les flammes avaient entour?? les arbres, mais des couronnes d'??toiles aux pi??ces d'eau??; les fontaines ??taient des volcans et les montagnes des mines d'or??; les canaux et les bassins s'en imbibaient avidement, reproduisaient les dessins et les doublaient??; et arbres, pi??ces d'eau, fontaines, montagnes, canaux et bassins semblaient rouler un immense incendie. Les clameurs du peuple, jointes au bruit des cascades et au fr??missement des arbres, vivifiaient ce splendide spectacle par leur majestueuse harmonie??: c'??tait la voix de Circ??, c'??tait le chant des sir??nes.</p>
               <p>?? onze heures du soir, tout l'Olympe descendit ?? terre??; de longues files de voitures serpentaient dans les jardins, et les resplendissantes dames de la cour qui les occupaient, pareilles ?? des files de perles, semblaient un r??ve de po??te, tant elles ??taient l??g??res et presque transparentes. Et, moi-m??me, j'??tais une de ces sylphides??! J'avais une robe de brocart, ??? qu'on appelle ?? la cour, je ne sais pourquoi, robe russe, ??? avec un dessous de satin blanc, garni de piqu??s d'or??; cette robe, ma ch??re Sophie, ??tait si bien coup??e, si bien brod??e, qu'avant de la v??tir, j'eus envie de me mettre ?? genoux devant??; j'??tais coiff??e avec des marabouts, pr??sent de mon mari, et je te dirai, sans vanit?? aucune, que cette coiffure m'allait ?? merveille??; et, quand m??me je ne m'en fusse pas rapport??e ?? mon miroir, le murmure des hommes sur mon passage e??t pu convaincre l'ap??tre Thomas lui-m??me que ta cousine ??tait tr??s gentille.</p>
           </div>
       </div>
       </body></text></TEI>
        """

        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)

        parser.parse()
        p this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        p this.jdbcTemplate.queryForList("select head from tei_div;", String.class)

        def list = this.jdbcTemplate.queryForList("select head from tei_div;", String.class)
        assert list.size() == 2
        assert list.first() == 'La princesse Flora'
        assert list.find {it.contains('[')} == null
        assert list.get(1) == 'I La princesse Flora ?? sa parente, ?? Moscou.'

        // also assert hierarchy is kept
        def div2 = this.teiDivRepository.findByHead('I La princesse Flora ?? sa parente, ?? Moscou.').first()
        def div1 = this.teiDivRepository.findByHead('La princesse Flora').first()
        assert div2.parent.head == div1.head
    }

    @Test
    void spaceAtTheBeginningOfHead() {
        final tei = teiOf("""
            <div type="div2" rend="P16">
               <head> ????????????????Cuv??nt ??nainte.</head>
               <subtitle> ???Uraganul ridicat de semilun??.???</subtitle>
               <p rend="justify"> ????????????????S-a ??mplinit, Ia 1</p>
           </div>
        """)
        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)
        parser.parse()

        p this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        final parsedHead = this.jdbcTemplate.queryForList("select head from tei_div;", String.class)[0]
        p parsedHead
        assert parsedHead == 'Cuv??nt ??nainte.'

    }

    /**
     * if I marked an upper-level head with /x/, do not recurse into its
     * div children, even if they are not marked with /x/
     */
    @Test
    void subchapterOfXdHeadWillNotBeRecursedinto() {
        final tei = teiOf("""
            <div>
                <head>this is /x/'d out</head>
                <div>
                    <head>this is a subchapter and should not be recursed into</head>
                    <p> some content</p>
                </div>
            </div>
        """)
        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)
        parser.parse()

        p this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        assert 0 == this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
    }

    @Test
    void figureRemovedFromHead() {
        final tei = teiOf("""
            <div type="div1" rend="P9">
                <head>
                    <figure>
                        <binaryObject xml:id="d3e3096" encoding="base64" mimeType="image/jpg">iVBORw0KGgoAAAANSUhEUgAAAP0AAAFeCAIAAADBqI5fAAAAAXNSR0IArs4c6QAAAAlwSFlz
                            AAALEwAACxMBAJqcGAAA/7VJREFUeF7s3dezdVlZ73ERc845ZwSbJDQ0NI3QtICCFGV55
                        </binaryObject>
                    </figure>
                    Pula calului
                </head>
            </div>
        """)
        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)
        parser.parse()

        p this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        assert 1 == this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        assert "Pula calului" ==  this.jdbcTemplate.queryForList("select head from tei_div;", String.class).get(0)

    }

    @Test
    void binaryObjectRemovedFromHead() {
        final tei = teiOf("""
            <div type="div1" rend="P9">
                <head>
                    <binaryObject xml:id="d3e3096" encoding="base64" mimeType="image/jpg">iVBORw0KGgoAAAANSUhEUgAAAP0AAAFeCAIAAADBqI5fAAAAAXNSR0IArs4c6QAAAAlwSFlz
                        AAALEwAACxMBAJqcGAAA/7VJREFUeF7s3dezdVlZ73ERc845ZwSbJDQ0NI3QtICCFGV55
                    </binaryObject>
                    Pula calului
                </head>
            </div>
        """)
        final parser = new ParseTeiFileIntoDb(tei,
                this.authorRepository,
                this.teiFileRepository,
                this.teiDivRepository,
                this.authorStrIdComputer)
        parser.parse()

        p this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        assert 1 == this.jdbcTemplate.queryForObject("select count(*) from tei_div;", Integer.class)
        assert "Pula calului" ==  this.jdbcTemplate.queryForList("select head from tei_div;", String.class).get(0)

    }



}

