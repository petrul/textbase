package ro.editii.scriptorium.toc

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository
import ro.editii.scriptorium.model.Author
import ro.editii.scriptorium.model.TeiDiv
import ro.editii.scriptorium.tei.AuthorStrIdComputer
import ro.editii.scriptorium.tei.ParseTeiFileIntoDb

import static ro.editii.scriptorium.GTestUtil.p

class TocTest {

    @Test
    void testToc() {

        def autorRep = Mockito.mock(AuthorRepository)
        def teifileRep = Mockito.mock(TeiFileRepository)
        def teidivRep = Mockito.mock(TeiDivRepository)

        Author author = new Author(id: 1, firstName: 'Vasile', lastName: 'Alecsandri')
        Mockito
                .when(autorRep.getByOriginalNameInTeiFile('Alecsandri,Vasile'))
                .thenReturn(new Optional<>(author))

        List<TeiDiv> divs = []

        Mockito
                .when(teidivRep.saveAll(Mockito.any(List<TeiDiv>))).thenAnswer(new Answer<List<TeiDiv>>() {
            @Override
            List<TeiDiv> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.arguments
                def batch = args[0]
                divs.addAll(batch)
                return batch
            }
        })

        p "*" * 80
        p divs

        URL res = this.getClass().getClassLoader().getResource("testrepo/ro/Alecsandri-Scrieri.xml")
        def parser = new ParseTeiFileIntoDb("Alecsandri-Scrieri.xml", res.openStream(),
            res,
            autorRep,
            teifileRep,
            teidivRep,
            new AuthorStrIdComputer()
        )

        parser.parse()

        def opuses = divs.findAll{ it.parent == null}
        def heads = divs.collect{it.head}

        opuses.each { op ->
            assert op.children != null
            assert op.children.size() > 0
            Toc toc = new Toc(op)
            println toc.asList().collect{it.head}.join("\n\t")
        }

        def nicolae_balcescu = divs.find{it.head == "Nicolae Bălcescu"}
        def constantin_Negruzzi = divs.find{it.head == "Constantin Negruzzi"}
        def merimee = divs.find{it.head == 'Prosper Mérimée'}
        def negruzzi_v = constantin_Negruzzi.children.find{ it.head == 'V'}
        def negruzzi_i = constantin_Negruzzi.children.find{ it.head == 'I'}
        def merimee_i = merimee.children.find { it.head == 'I'}

        def biografii = opuses.find {it.head == 'Biografii'}
        def toc = new Toc(biografii)
        def toc_heads = toc.collect {it.head}

        assert toc_heads == [
                "Biografii",
                        "Alecu Russo",
                            "I",
                            "II",
                            "III",
                        "Nicolae Bălcescu",
                        "Constantin Negruzzi",
                            "I",
                            "II",
                            "III",
                            "IV",
                            "V",
                        "Prosper Mérimée",
                            "I",
                            "II",
                            "III"
        ]

        assert toc.prev(constantin_Negruzzi) == nicolae_balcescu
        assert toc.next(constantin_Negruzzi) == negruzzi_i
        assert toc.next(nicolae_balcescu) == constantin_Negruzzi
        assert toc.prev(merimee) == negruzzi_v
        assert toc.next(merimee) == merimee_i
        assert toc.prev(merimee_i) == merimee

    }
}

