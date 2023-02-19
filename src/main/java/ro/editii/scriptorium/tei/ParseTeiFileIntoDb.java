package ro.editii.scriptorium.tei;

import editii.commons.xml.DomTool;
import editii.commons.xml.XpathTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.dao.TeiFileRepository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.model.TeiFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TOC = table of contents. Basically parses a tree of divs of a TEI and inserts results into db entities.
 */
public class ParseTeiFileIntoDb {

    final TeiFileRepository teiFileRepository;
    final AuthorRepository authorRepository;
    TeiDivRepository teiDivRepository;

    String filename;
    final private AuthorStrIdComputer authorStrIdComputer;

    XpathTool xpathTool;
    protected Map<Node, TeiDiv> node2div = new LinkedHashMap<>();

    public ParseTeiFileIntoDb(String content,
                              AuthorRepository authorRepository,
                              TeiFileRepository teiFileRepository,
                              TeiDivRepository teiDivRepository,
                              AuthorStrIdComputer authorStrIdComputer) {
            this(Integer.valueOf(content.hashCode()).toString(),
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    Integer.valueOf(content.hashCode()).toString(),
                    authorRepository, teiFileRepository, teiDivRepository,
                    authorStrIdComputer);
    }


    /**
     * @param filename will be recourded in the database as the source where the parsed xml comes from
     * @param url needed to get the systemId (identifier of the source for the xpath builder
     */
    public ParseTeiFileIntoDb(String filename,
                              InputStream is,
                              URL url,
                              AuthorRepository authorRepository,
                              TeiFileRepository teiFileRepository,
                              TeiDivRepository teiDivRepository,
                              AuthorStrIdComputer authorStrIdComputer) {
        this(filename, is, url.toExternalForm(), authorRepository, teiFileRepository, teiDivRepository, authorStrIdComputer);
    }


    /**
     * @param filename will be recorded in the database as the source where the parsed xml comes from
     * @param systemId a sort of identifier of the content. Use URL.toExternalForm()
     */
    public ParseTeiFileIntoDb(String filename,
                              InputStream is,
                              String systemId,
                              AuthorRepository authorRepository,
                              TeiFileRepository teiFileRepository,
                              TeiDivRepository teiDivRepository,
                              AuthorStrIdComputer authorStrIdComputer) {


        this.filename = filename;
        this.xpathTool = new XpathTool(is, systemId);
        this.authorStrIdComputer = authorStrIdComputer;

        this.authorRepository = authorRepository;
        this.teiFileRepository = teiFileRepository;
        this.teiDivRepository = teiDivRepository;
    }

    private String xpath(String str_xpath) {
        return this.xpathTool.xpath(str_xpath);
    }

    private Collection<Node> xpath2Nodes(String strXpath) {
        return DomTool.nodeList2Collection(this.xpathTool.applyXpathForNodeSet(strXpath));
    }


    /**
     * the Author is reused if existing.
     * the TeiFile, it depends on the forceReimport flag :
     *  - if true, a deletion of the TeiFile and all sousjacent data
     *  - if false, a checked exception should be thrown
     */
    public void parse() throws TeiFileAlreadyImportedException {

        // this should be smth like Alecsandri,Vasile
        String authorName = xpath("/tei:TEI/tei:teiHeader//tei:titleStmt/tei:author");

        Author author = Author.newFromOriginalNameInTeiFile(authorName);

        // check existing author already in db
        Optional<Author> authorOptionalRetrieved = this.authorRepository.getByOriginalNameInTeiFile(author.getOriginalNameInTeiFile());
        if (authorOptionalRetrieved.isPresent()) {
             // already an originalname present in db
            author = authorOptionalRetrieved.get();
        } else {
            // no such original name in db
            this.authorStrIdComputer.compute_strid_for_new_author(author);
            this.authorRepository.save(author);
        }

        // teiFile
        String teiFilename = this.filename;
        TeiFile teifile = new TeiFile();

        teifile.setFilename(teiFilename);
        teifile.setTitle(xpath("/tei:TEI/tei:teiHeader//tei:titleStmt/tei:title/text()").trim());
        teifile.setAuthors(new ArrayList<>(Arrays.asList(author)));

        // check if already existing teiFile
        Optional<TeiFile> optionalTeiFile = this.teiFileRepository.getByFilename(teiFilename);

        if (optionalTeiFile.isPresent())
            throw new TeiFileAlreadyImportedException("teifile " + teiFilename + " already imported");

        this.teiFileRepository.save(teifile);

        Collection<Node> root_divs = xpath2Nodes("//tei:text/tei:body/tei:div");

        LOG.info("{} root divs", root_divs.size());
        this.node2div = new LinkedHashMap<>();// reinit

        for (Node node : root_divs) {
            List<TeiDiv> acc = new ArrayList<>(1000);
            parcurge_rec(node, null, teifile, acc);
            if (acc.size() > 0) {
                this.teiDivRepository.saveAll(acc);
                acc.clear();
            }
        }
    }

    protected String compute_unique_head_url_fragment(TeiDiv div) {

        TeiDiv parentDiv = div.getParent();

        CandidateUrlFragmGeneratorForTeiDivHead iterable = new CandidateUrlFragmGeneratorForTeiDivHead(div.getHead());
        for (String candidate : iterable) {

            // check for the case where another edition of the same work, already exists imported into the db
            if (parentDiv == null) {
                Author author = div.getTeiFile().getAuthor();
                List<TeiDiv> opuses = this.teiDivRepository.findOperaForAuthorStrId(author.getStrId());
                if (opuses
                        .stream()
                        .anyMatch( it -> candidate.equals(it.getUrlFragment()))) {
                    // here we found another opus already imported, of the same author bearing the same name, probably another edition.
                    continue; // next candidate
                }
            }

            // if no other child has the same urlFragment, then return it as good
            if (parentDiv == null
                    || parentDiv.getChildren() == null
                    || parentDiv.getChildren()
                    .stream()
                    .noneMatch( it -> candidate.equals(it.getUrlFragment())))
                return candidate;
        }
        throw new IllegalStateException("should never get here, iterator is infinite");
    }

    public void parcurge_rec(final Node node, Node parent, final TeiFile teiFile, List<TeiDiv> acc) {

        TeiDiv parentDiv;
        if (parent == null)
            parentDiv = null;
        else
            parentDiv = this.node2div.get(parent);

        assert "div".equalsIgnoreCase(node.getNodeName());

        NodeList nodeList = node.getChildNodes();
        List<Node> children = (List) DomTool.nodeList2Collection(nodeList);

        // filter out comments and empty text children.
        children = children.stream()
                .filter( it -> it.getNodeType() != Node.COMMENT_NODE)
                .filter( it -> ! (
                            it.getNodeType() == Node.TEXT_NODE
                            && it.getTextContent().trim().equals("")
                        ))
                .collect(Collectors.toList());

        String head = this.getHead(node);

        if (isMarkedWithX(head))
            return;

        if (head == null || head.isEmpty()) {
            // ignore this div
            // divs with empty head are generated by the odttotei when you have a h3 under a h1 for example.

        } else {
            // normal div

            final TeiDiv div = new TeiDiv();
            try {
                head = head.substring(0, TeiDiv.MAX_HEAD_SIZE); // max head size
            } catch (IndexOutOfBoundsException e) { /* ignore */ }

            div.setHead(head);
            div.setParent(parentDiv);
            div.setTeiFile(teiFile);

            String urlFragm = this.compute_unique_head_url_fragment(div);
            String xpath = XpathTool.getXPath(node);
            xpath = xpath.substring("/tei:TEI/tei:text/tei:body".length());

            if (parentDiv != null)
                parentDiv.addChild(div);


            div.setUrlFragment(urlFragm);
            div.setXpath(xpath);

            final String textContent = new XpathTool(node).getTextContentRec();
            final String trimmed = textContent
                    .replaceAll("\\s+", " ")
                    .trim();
            final int size = trimmed.length();
            final int wordSize = trimmed.split("\\s+").length;
            div.setSize(size);
            div.setWordSize(wordSize);

            this.node2div.put(node, div);

            if (acc.size() >= 1000) {
                this.teiDivRepository.saveAll(acc); // use saveAll so inserts be batch'd
                acc.clear();
            }
            acc.add(div);

            LOG.info(div.toString());

            parent = node;

        }

        // recurse to children

        for (Node child: children) {
            if ("div".equalsIgnoreCase(child.getNodeName())) {
                parcurge_rec(child, parent, teiFile, acc);
            }
        }
    }

    public static void replaceLbWithBlank(Node node, int level) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeName().equals("lb")) {
                Node whitespace = node.getOwnerDocument().createTextNode(" ");
                childNode.getParentNode().replaceChild(whitespace, childNode);
            }

            replaceLbWithBlank(childNode, level + 1);
        }
    }

    /**
     * some head's contain a <label> which will not be used for url fragm generation</label>
     */
//    protected static void removeLabel(Node node) {
//        DomTool.removeSubnodeByNodename(node, "label");
//    }

    protected String getHead(Node div) {
        XpathTool divXpathTool = new XpathTool(div, true);
        NodeList headSet = divXpathTool.applyXpathForNodeSet("/tei:div/tei:head");
        if (headSet.getLength() < 1)
            return null;
        List<String> headBuilder = new ArrayList<>(1); // most only have one head

        // there should only be one head, but make it work even if there are several
        for (int h = 0; h < headSet.getLength(); h++) {
            Node headNode = headSet.item(h);
            headNode = DomTool.deepCopy(headNode);

            // TODO do this on head; right now the next two lines are applied to the whole div
            replaceLbWithBlank(headNode, 0);

            // strange things that you might occasionally find in a <head>
            DomTool.removeSubnodesByNodenames(headNode, new String[] {"label", "note", "figure", "binaryObject"});

            StringBuilder sb = new StringBuilder();
            /* because there is an apparent bug in the java dom :
             * XPathTool.applyXpathForNodeSet(".//text()")
             * after removeChild, so use the following workaround
             */
            NodeList nodeSet = XpathTool.from(headNode, true).select(it -> it.getNodeType() == Node.TEXT_NODE);
            for (int i = 0; i < nodeSet.getLength(); i++) {
                Node crt = nodeSet.item(i);
                final String crtText = crt.getTextContent();
                final String whitespaceRemoved = this.removeWhitespace(crtText);
                if (whitespaceRemoved.isEmpty() && crtText.contains("\n")) {
                    // interstitial whitespace between tags, just ignore them
                } else {
                    sb.append(crtText);
                }
            }

            String head = this.nbspToSpace(sb.toString())
                    .replaceAll("\\n", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (head != null && !head.isEmpty())
                headBuilder.add(head);

        }

        return String.join(" ", headBuilder);
    }

    // replace &nbsp; with regular trimmable space
    String nbspToSpace(String s) {
        return s.replaceAll("\u00A0", " ");
    }

    String removeWhitespace(String s) {
        return s.replaceAll("\\n", "")
                .replaceAll("\\s+", "");
    }


    /**
     * @return true if head contins something like /x/ or [x], case ignored
     */
    protected static boolean isMarkedWithX(String head) {
        if (head == null)
            return false;
        return head.matches("(?i)^.*?[\\[|/]x+[\\]|/].*$");
    }

    final private static Logger LOG = LoggerFactory.getLogger(ParseTeiFileIntoDb.class);
}
