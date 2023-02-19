package ro.editii.scriptorium.lucene

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.ro.RomanianAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import ro.editii.scriptorium.Util

import java.nio.file.Paths

@Deprecated()
class Indexer {

    static void main(String[] args) {
//        println System.getProperties().find { it =~ /home/}
//        def tmpdir = System.properties['java.io.tmpdir']
        def indexdir = new File(Util.getAppDotDir(), "my-index")
        println "will use $indexdir ..."

        Directory dir = FSDirectory.open(Paths.get(indexdir.toURI()))
        Analyzer analyzer = new RomanianAnalyzer();
        IndexWriterConfig cfg = new IndexWriterConfig(analyzer)
        def iw = new IndexWriter(dir, cfg)


    }
}
