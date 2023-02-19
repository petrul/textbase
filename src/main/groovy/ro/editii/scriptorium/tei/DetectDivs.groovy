package ro.editii.scriptorium.tei

// this is obsolete, i tried to use groovy gpath, but regular dom java xpath is just fine

//@Grapes([
// @Grab('org.slf4j:slf4j-simple:1.5.11'),
// @Grab('mysql:mysql-connector-java:5.1.12'),
//// @GrabConfig(systemClassLoader = false)
//])
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.sql.Sql
import groovy.xml.XmlUtil
 
// def ns = new groovy.xml.Namespace("http://www.tei-c.org/ns/1.0", "tei")

//def sql_host="mini"
//def sql_db="scriptorium_dev"
// def sql_username = "scriptorium_dev"
// def sql_password = "styaccutVocItit"

def sql_host="localhost"
def sql_db="dev"
def sql_username = "dev"
def sql_password = "dev"

def sql = Sql.newInstance(
    "jdbc:mysql://${sql_host}:3306/${sql_db}", 
    sql_username,
    sql_password, 
    "com.mysql.jdbc.Driver");

sql.rows('select * from author').each { println it }
sql.rows('select * from tei_div').each { println it }

Collection<File> inspect_dir(teidir) {
    def list = new File(teidir).list().findAll {
        new File(teidir, it).isFile()
    }

    return list
        .findAll { ! it.startsWith(".")  && it.endsWith(".xml")}
        .collect{ new File(teidir, it) }
}

def depth(NodeChild node) {
    if (node.parent() != node)
        return 1 + depth(node.parent())
    else
        return 1;
}

def extract_author_title(GPathResult doc) {
    return doc[t.TEI][t.teiHeader]    //.'**'.findAll { it.name() == 'author' }
}

def detect_divs(GPathResult doc) {

//    def heads = doc.'**'.findAll { it.name() == 'head'}*.text()

    final Collection divs = doc.'**'.findAll { it.name() == 'div' }
    println divs.size()
    divs.each { NodeChild d ->
        head = d.'*'.findAll { it.name() == 'head' }

        def depth = depth(d)
        def indent = "\t" * (depth)
        println "$indent$head ($depth) "

    }
}

def teidir = System.getProperty('user.home') + "/work/scriptorium-masters/tei"
teis = this.inspect_dir(teidir)

println "will parse ${teis.size} files: \n" + teis.join('\n')

// def file = new File(teidir, "Zamfirescu,Duiliu-Tanase_Scatiu,Viata_la_tara.xml")
// def doc = parse_xml(file)
// detect_divs(doc)


GPathResult parse_xml(File f) {
    GPathResult doc = new XmlSlurper()
            .parse(f)
            .declareNamespace(t: "http://www.tei-c.org/ns/1.0")
    assert doc.'/t:TEI' != null
    println "parsed " + XmlUtil.serialize(doc.'/t:TEI') //.getClass()
    assert doc.'/t:TEI/t:teiHeader' != null
    // println "parsed " + XmlUtil.serialize(doc.'/t:TEI/t:teiHeader') //.getClass()
    // assert doc.
    return doc
    
}

teis.each { t ->
    println '*' * 10 + " [$t] " + "x" * 20
    def xml = parse_xml(t)
    // println XmlUtil.serialize(xml)
    println XmlUtil.serialize(extract_author_title(xml))
    // detect_divs(xml)
}