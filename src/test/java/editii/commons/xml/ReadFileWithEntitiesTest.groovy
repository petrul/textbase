package editii.commons.xml

import org.junit.jupiter.api.Test
import org.w3c.dom.NodeList

class ReadFileWithEntitiesTest {

    @Test
    void testXIncludeWorks() {
        def bible_xml = this.getClass().getResource("bible.xml")
        def is = bible_xml.openStream()
        assert is.text.contains("<xi:include")

//        println "=" * 80

        is = bible_xml.openStream()
        def xpath = new XpathTool(is, bible_xml.toURI().toASCIIString())
        NodeList nodeSet = xpath.applyXpathForNodeSet("/TEI")

        assert 'La început a făcut Dumnezeu cerul şi pământul.' == xpath.xpath("//div[@id='Fac']//div[1]/p[1]")

    }
}
