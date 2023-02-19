package editii.commons.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DomTool {

    public static Collection<Node> nodeList2Collection(NodeList nodeList) {
        ArrayList<Node> arr = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            arr.add(nodeList.item(i));
        }
        return arr;
    }

    public static void serialize(Node node, OutputStream os) {

        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(node), new StreamResult(os));
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serialize(Node node) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(node, baos);
        try {
            baos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return baos.toString("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString(Node node) {
        return serialize(node);
    }

    public static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Node deepCopy(Node node) {
        final Document document = newDocument();
        return document.importNode(node, true);
    }

    public static Node rootForResults (NodeList nodeList) {
        if (nodeList.getLength() < 2)
            return nodeList.item(0);

        Document newXmlDocument;
        try {
            newXmlDocument = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Element root = newXmlDocument.createElement("results");
        newXmlDocument.appendChild(root);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node imported = newXmlDocument.importNode(node, true);
            root.appendChild(imported);
        }

        return root;
    }

    public static void removeSubnodeByNodename(Node node, String nodeName) {
        removeSubnodesByNodenames(node, new String[] { nodeName });
//        Collection<Node> children = DomTool.nodeList2Collection(node.getChildNodes());
//        List<Node> labelChildren = children.stream().filter(it -> it.getNodeName().equals(nodeName)).collect(Collectors.toList());
//
//        labelChildren.stream().forEach(it -> {
//            node.removeChild(it);
//        });
//
//        children = DomTool.nodeList2Collection(node.getChildNodes()); // again, because some might have been removed
//        children.stream().forEach(it -> removeSubnodeByNodename(it, nodeName));// recurse
    }

    /**
     * remove all descendent nodes whose names are in the nodeNames list
     * @param nodeNames list of names of nodes to remove
     */
    public static void removeSubnodesByNodenames(Node node, String[] nodeNames) {
        Collection<Node> children = DomTool.nodeList2Collection(node.getChildNodes());
        final List<String> strings = Arrays.asList(nodeNames);
        List<Node> namedChildren = children.stream()
                .filter(it -> strings.contains(it.getNodeName()))
                .collect(Collectors.toList());

        namedChildren.stream().forEach(it -> {
            node.removeChild(it);
        });

        children = DomTool.nodeList2Collection(node.getChildNodes()); // again, because some might have been removed
        children.stream().forEach(it -> removeSubnodesByNodenames(it, nodeNames));// recurse
    }

//    String getTextContentRec(Node node) {
//
//    }
}
