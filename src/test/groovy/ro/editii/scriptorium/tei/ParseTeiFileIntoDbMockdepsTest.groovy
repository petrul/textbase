package ro.editii.scriptorium.tei


import org.junit.jupiter.api.Test
import org.mockito.Mockito
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository
import ro.editii.scriptorium.model.TeiDiv

import static ro.editii.scriptorium.GTestUtil.*


class ParseTeiFileIntoDbMockdepsTest {

    /**
     * assert proper calculation of size
     */
    @Test
    void teiDivSize() {

        AuthorRepository authorRepository = Mockito.mock(AuthorRepository.class)
        TeiFileRepository teiFileRepository = Mockito.mock(TeiFileRepository.class)
        TeiDivRepository teiDivRepository = Mockito.mock(TeiDivRepository.class)

        final List<TeiDiv> teiDivs = []
        Mockito
            .when(teiDivRepository.saveAll(Mockito.anyCollection()))
            .thenAnswer( inv -> {
                List arg = inv.getArgument(0)
                teiDivs.addAll(arg)
                return arg
            })

        AuthorStrIdComputer authorStrIdComputer = new AuthorStrIdComputer(authorRepository)
        final cnt = """
            <div>
                <head>head</head>
                <p>pula1</p>
                <p>pula2</p>
            </div>
        """
        final tei = teiOf(cnt)
        final parser = new ParseTeiFileIntoDb(tei,
                authorRepository,
                teiFileRepository,
                teiDivRepository,
                authorStrIdComputer)
        parser.parse()

        assert teiDivs.size() == 1
        final teiDiv = teiDivs.first()
        assert teiDiv.size == "head pula1 pula2".length()
        assert teiDiv.wordSize == 3
    }
}