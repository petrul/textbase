package ro.editii.scriptorium.indexer

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.Term
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.QueryBuilder
import ro.editii.scriptorium.Util

import java.nio.file.Paths

class search {

    static void main(String[] args) {
        def indexPath = new File(Util.getAppDotDir(), "index").getAbsolutePath()
        String what = "eminescu"

        QueryBuilder qb = new QueryBuilder(new StandardAnalyzer())
        Query query = qb.createPhraseQuery("content", what)
//        Query query = new QueryBuilder() QueryParser(what, new StandardAnalyzer()).parse(what);
        Directory directory = FSDirectory.open(Paths.get(indexPath))
        IndexReader indexReader = DirectoryReader.open(directory)
        println "getdoccount" + indexReader.getDocCount("filename")

//        println MultiFields.getLiveDocs(indexReader)

        Terms terms = MultiFields.getTerms(indexReader, "content");
        println("terms" + terms)
//        TermsEnum termsEnum = terms.iterator()
//        println(termsEnum)

        IndexSearcher searcher = new IndexSearcher(indexReader)
        Term term = new Term("content", what)
        TermQuery tq = new TermQuery(term)
//        TopDocs topDocs = searcher.search(tq, 100)
        TopDocs topDocs = searcher.search(query, 100)

        println topDocs.totalHits;
        println topDocs.scoreDocs
        def dox = topDocs.scoreDocs.collect { scoreDoc -> searcher.doc(scoreDoc.doc) }
        println dox;
    }

}
