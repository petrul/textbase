package ro.editii.scriptorium.xslt;

import net.sf.saxon.TransformerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * reads a Dom and applyes different xslts
 */
public class XsltTool {

//    private final InputStream inputStream;
    private final Node root;

    public XsltTool(InputStream inputStream) {
        this.root = parseInputStreamToNode(inputStream);
    }

    public XsltTool(Node root) {
        this.root = root;
    }

    public static Node parseInputStreamToNode(InputStream inputStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = factory.newDocumentBuilder();
            return documentBuilder.parse(inputStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void applyXslt(InputStream xsl, String systemId, OutputStream outputStream) {
        this.applyXslt(xsl, systemId, Map.of(), outputStream);
    }

    /**
     * @param xsl
     * @param systemId the filename of the xsl, you need to specify this so that the xsl processor be able to resolve relative includes
     * @param outputStream
     */
    public void applyXslt(InputStream xsl, String systemId, Map<String, String> params, OutputStream outputStream) {

        DOMSource source = new DOMSource(this.root);
        StreamResult result = new StreamResult(outputStream);
        Transformer transformer = getTransformer(xsl, systemId);

        setParams(transformer, params);

        try {
            transformer.transform(source, result);
        } catch (javax.xml.transform.TransformerException e) {
            throw new RuntimeException(e);
        }

    }

    static void setParams(Transformer transformer, Map<String, String> params) {
        if (params != null) {
            params.keySet().forEach(it -> {
                transformer.setParameter(it, params.get(it));
            });
        }
    }


    public static Transformer getTransformer(InputStream xsl, String systemId) {
        StreamSource streamSource = new StreamSource(xsl, systemId);
        TransformerFactory tFactory = new TransformerFactoryImpl();

        try {
            Transformer transformer = tFactory.newTransformer(streamSource);
            return transformer;
        } catch (javax.xml.transform.TransformerException e) {
            throw new RuntimeException(e);
        }
    }


    public static void apply(Transformer transformer, Node xml, OutputStream outputStream, Map<String, String> params) {
        DOMSource source = new DOMSource(xml);
        StreamResult result = new StreamResult(outputStream);

        setParams(transformer, params);

        try {
            transformer.transform(source, result);
        } catch (javax.xml.transform.TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static String apply(Transformer transformer, Node xml, Map<String, String> params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        apply(transformer, xml, baos, params);

        String res = new String(baos.toByteArray());
        return res;
    }


    public static void apply(Transformer transformer, InputStream inputStream, Map<String, String> params, OutputStream outputStream) {
        Node root = parseInputStreamToNode(inputStream);
        apply(transformer, root, outputStream, params);
    }

    public static String apply(Transformer transformer, String xml) {
        return apply(transformer, xml, Map.of());
    }

    public static String apply(Transformer transformer, String xml, Map<String, String> params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        setParams(transformer, params);

        apply(transformer, is, params, baos);

        String res = new String(baos.toByteArray());
        return res;
    }

    public String applyXsltForString(InputStream xsl, String systemId) {
        return this.applyXsltForString(xsl, systemId, Map.of());
    }

    public String applyXsltForString(InputStream xsl, String systemId, Map<String, String> params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.applyXslt(xsl, systemId, params, baos);
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String applyXslt(File file, Map<String, String> params) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            return this.applyXsltForString(bufferedInputStream, file.toURI().toString(), params);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRawText() throws IOException {
        return this.getRawText(Map.of());
    }

    public String getRawText(Map<String, String> params) throws IOException {
        URL xsltResource = this.getClass().getClassLoader().getResource("xslt/xml2text.xsl");
        try {
            String res = this.applyXsltForString(xsltResource.openStream(), xsltResource.toURI().toString(), params);
            return res;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    final private static Logger LOG = LoggerFactory.getLogger(XsltTool.class);
}
