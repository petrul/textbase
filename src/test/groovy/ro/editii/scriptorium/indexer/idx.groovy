package ro.editii.scriptorium.indexer

import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import ro.editii.scriptorium.Util
import ro.editii.scriptorium.tei.TeiDirRepoImpl
import ro.editii.scriptorium.tei.TeiRepo

import java.nio.file.Paths

class idx {

    static void main(String[] args) {
//        def indexPath = "/tmp/index"
        def indexPath = new File(Util.getAppDotDir(), "index").toURI()

        def analyzer = new StandardAnalyzer()

        IndexWriterConfig indexWriterConfig= new IndexWriterConfig(analyzer);

        Directory directory = FSDirectory.open(Paths.get(indexPath))
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        indexWriter.deleteAll()

        TeiRepo teiRepo = new TeiDirRepoImpl("/Users/petru/scriptorium-masters/build")

        [teiRepo.list().first()].each {
            println "file $it"
            Document document = new Document();



            FileReader fileReader = new FileReader(teiRepo.getFile(it));

            def slurper = new XmlSlurper().parse(fileReader)

//            def text = teiRepo.getFile(it).text
//            println text

            def paragraphs = slurper.'**'.findAll { NodeChild node ->
                node.name() == 'lg' || node.name() == 'p'
            }

            println slurper.getClass()
            println paragraphs.getClass()
            println paragraphs.size()
            println "*" * 200
            paragraphs.each {  NodeChild node ->
                println node.name() + ": " + node.text()
            }

//            document.add(new TextField("contents", fileReader));
//            document.add(new StringField("filename", it, Field.Store.YES));
//
//            indexWriter.addDocument(document);

        }


        indexWriter.close();

    }
}
