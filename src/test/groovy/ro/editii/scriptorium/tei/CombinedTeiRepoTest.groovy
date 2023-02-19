package ro.editii.scriptorium.tei

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import ro.editii.scriptorium.GTestUtil

class CombinedTeiRepoTest {

    @Test
    void testFilter() {

        final root = GTestUtil.tmpDir()
        new File(root).deleteOnExit()

        final todelete = []

        println(root)
        todelete << root
        final dirA = new File(root, "a")
        final dirB = new File(root, "b")
        FileUtils.forceMkdir(dirA)
        FileUtils.forceMkdir(dirB)

        todelete << dirA << dirB

        (1..10).each {
            final f = new File(dirA, "file-a${it}.xml");
            f.deleteOnExit()
            f.text = "aaaa $it"
            todelete << f
        }
        (1..10).each {
            final f = new File(dirB, "FILE-b${it}.xml");
            f.deleteOnExit()
            f.text = "bbbb $it"
            todelete << f
        }

        def repo = new CombinedTeiRepo(root)
        assert repo.list().size() == 20
        repo = CombinedTeiRepo.builder()
            .withDirs(root)
            .withFilter("1")
            .build();

        assert repo.list().size() == 4

        repo = CombinedTeiRepo.builder()
                .withDirs(root)
                .withFilter("10")
                .build();

        assert repo.list().size() == 2

        repo = CombinedTeiRepo.builder()
                .withDirs(root)
                .withFilter("file-a10")
                .build();
        assert repo.list().size() == 1

        repo = CombinedTeiRepo.builder()
                .withDirs(root)
                .withFilter("FILE-a10")
                .build();
        assert repo.list().size() == 1

        repo = CombinedTeiRepo.builder()
                .withDirs(root)
                .withFilter("file-b10")
                .build();
        assert repo.list().size() == 1

        todelete.reverse().each {
            final f = it instanceof File ? it : new File(it)
            f.delete()
        }
    }
}