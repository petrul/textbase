package ro.editii.scriptorium.tei

import org.junit.jupiter.api.Test
import ro.editii.scriptorium.Util

class TeiDirRepoImplTest {

    @Test
    void listWithFilter() {
        final String dirname = Util.urlToFileString(this.class.classLoader.getResource("testrepo"))
        p dirname
        def repo = new TeiDirRepoImpl(dirname)
        p repo.list()
        assert repo.list().size() == 4
        repo = new TeiDirRepoImpl(dirname, "unexisting strings")
        assert repo.list().size() == 0

        repo = new TeiDirRepoImpl(dirname, "(?i)alecsa")
        assert repo.list().size() == 2

        // current behavior is to make it case-insensitive
        repo = new TeiDirRepoImpl(dirname, "alecsa")
        assert repo.list().size() == 2

        repo = new TeiDirRepoImpl(dirname, "Alecsa")
        assert repo.list().size() == 2
    }

    def p(args) { println(args) }

}