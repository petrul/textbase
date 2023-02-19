package editii.commons.xml

import org.junit.jupiter.api.Test
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


class PlayWithXmlNamespacesTest {


    /**
     * see how to parse an xml with java dom parser
     */
    @Test
    void testParseXmlWithNamespaces() {
        URL resource = this.getClass().getClassLoader().getResource("testrepo/ro/Alecsandri-Scrieri.xml");
        resource.openStream()

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance()
        documentBuilderFactory.setNamespaceAware(true)
        XPathFactory xPathFactory = XPathFactory.newInstance()

        def builder = documentBuilderFactory.newDocumentBuilder()
        def doc = builder.parse(resource.openStream(), resource.toURI().toASCIIString())

        def root = doc.getDocumentElement()
        println root.getNodeName()
        println root.getNamespaceURI()

        def xpath = xPathFactory.newXPath()
        xpath.setNamespaceContext(new MapNamespaceResolver([ "tei" : "http://www.tei-c.org/ns/1.0"]))

//        def compiled = xpath.compile("/:TEI//:teiHeader")
//        assert compiled.evaluate(root, XPathConstants.NODESET).length == 1

        def compiled = xpath.compile("/tei:TEI//tei:teiHeader")
        println compiled.evaluate(root, XPathConstants.NODESET).length
        assert compiled.evaluate(root, XPathConstants.NODESET).length == 1
        Element item = compiled.evaluate(root, XPathConstants.NODESET).item(0)
        assert item.nodeName == "teiHeader"
        assert item.namespaceURI == "http://www.tei-c.org/ns/1.0"
    }
}

//class JustTeiNamespCtxt implements NamespaceContext {
//    @Override
//    String getNamespaceURI(String prefix) {
//        if (prefix == "tei")
//            return "http://www.tei-c.org/ns/1.0"
//        else
//            throw new RuntimeException()
//    }
//
//    @Override
//    String getPrefix(String namespaceURI) {
//        if (namespaceURI == "http://www.tei-c.org/ns/1.0")
//            return "tei"
//        else
//            throw new RuntimeException()
//    }
//
//    @Override
//    Iterator getPrefixes(String namespaceURI) {
//        throw  new RuntimeException("unimplemented")
//    }
//}