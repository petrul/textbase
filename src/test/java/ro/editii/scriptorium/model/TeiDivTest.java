package ro.editii.scriptorium.model;

import editii.commons.xml.XpathTool;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
public class TeiDivTest {

    @Test
    public void cacheBug() throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("testrepo/ro/Alecsandri-Scrieri.xml");
        InputStream is = resource.openStream();
        String xpath = "/tei:TEI/tei:text/tei:body/tei:div[4]/tei:div/tei:div[4]";
        XpathTool xpathTool = new XpathTool(is, resource.toURI().toASCIIString());
        final String res = xpathTool.xpath(xpath);
        System.out.println(res);

    }
}