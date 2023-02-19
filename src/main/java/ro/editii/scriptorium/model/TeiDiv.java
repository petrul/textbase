package ro.editii.scriptorium.model;

import editii.commons.xml.DomTool;
import editii.commons.xml.XpathTool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import org.apache.commons.collections4.map.LRUMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.editii.scriptorium.tei.TeiRepo;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * a <div> inside a TEI (may be a fragment, a work, or something)
 */
@Entity
@Table(indexes = {@Index(columnList = "urlFragment")})
@Log
@Data
@NoArgsConstructor
public class TeiDiv implements Comparable<TeiDiv> {

    final public static String TABLE_NAME = "tei_div";
    final public static int MAX_HEAD_SIZE = 3000;
    final public static int MAX_URL_FRAGM_SIZE = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    TeiFile teiFile;

    @ManyToOne
    @ToString.Exclude
    TeiDiv parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    List<TeiDiv> children;

    @Size(max = MAX_URL_FRAGM_SIZE) @Column(length = MAX_URL_FRAGM_SIZE)
    String urlFragment;

    @Size(max=MAX_HEAD_SIZE) @Column(length = MAX_HEAD_SIZE)
    String head;

    String xpath;

    int size; // approximate size of this div in characters
    int wordSize; // approx size in words (continuous non-space chars)

    @Lob @EqualsAndHashCode.Exclude @ToString.Exclude
    byte[] image;

    @Transient @EqualsAndHashCode.Exclude @ToString.Exclude
    int depth = -1;
    public int getDepth() {
        if (depth < 0) {
            if (this.parent == null)
                this.depth = 0;
            else
                this.depth = 1 + this.parent.getDepth();

        }
        return this.depth;
    }

    @Transient @ToString.Exclude @EqualsAndHashCode.Exclude
    TeiRepo teiRepo; // this is not stored but it is needed for getNode()

    // this is the concatenated urlFragment of this and its ancestors -- starting from author really.
    public String getUrl() {
        if (this.parent == null)
            return this.getUrlFragment();
        else
            return this.getParent().getUrl() + "/" + this.getUrlFragment();
    }

    // this is really the complete _path_ that follows after the base /
    public String getCompletePath() {
        return this.getAuthor().getStrId() + "/" + this.getUrl();
    }


    public void addChild(TeiDiv teiDiv) {
        if (this.children == null)
            this.children = new ArrayList<>();
        this.children.add(teiDiv);
    }

    public Author getAuthor() {
        return this.getTeiFile().getAuthor();
    }

    // parsing a tei file is expensive so cache so we don't parse a new file for every div.
    final static LRUMap<String, XpathTool> cache_parsedFiles = new LRUMap<>(10, 10);

    protected XpathTool get_parsed_file(final String filename) {
        synchronized (cache_parsedFiles) {
            final XpathTool cached = cache_parsedFiles.get(filename);
            if (cached != null) {
                return cached;
            }

            log.info("parsing " + filename + " from thread " + Thread.currentThread().getName());
            InputStream is = teiRepo.getStreamForName(filename);
            XpathTool xpathTool = new XpathTool(is, filename); // this parses the file too
            cache_parsedFiles.put(filename, xpathTool);

            return xpathTool;
        }
    }

    @Transient
    Node _node = null; // cache
    public Node getNode() {
        if (_node != null)
            return _node;

        if (this.teiRepo == null)
            throw new IllegalStateException("you must set a TeiRepo in order to retrieve the file content of teiFile");

        XpathTool xpathTool = get_parsed_file(teiFile.getFilename());

        String xpath_expr = "/tei:TEI/tei:text/tei:body" + this.getXpath();
        if (xpath_expr.endsWith("/")) // remove trailing "/"
            xpath_expr = xpath_expr.substring(0, xpath_expr.length() - 1);

        final NodeList nodeList = xpathTool.applyXpathForNodeSet(xpath_expr);

        if (nodeList.getLength() != 1)
            throw new IllegalStateException(
                String.format(
                    "Expected exactly one div to correspond to xpath [%s], instead got %s",
                    xpath_expr,
                    nodeList.getLength() == 0 ? "none" : Integer.toString(nodeList.getLength()))
            );
        this._node = DomTool.deepCopy(nodeList.item(0));

        return this._node;
    }

    public String getBody() {
        return DomTool.serialize(this.getNode());
    }

    public String getLanguage() {
        final XpathTool xp = new XpathTool(this.getNode());
        return xp.xpath("/tei:TEI/tei:teiHeader//tei:profileDesc//tei:language");
    }

    public String getLicense() {
        return new XpathTool(getNode()).xpath("/tei:TEI/tei:teiHeader//tei:publicationStmt");
    }

    public Node getSourceDesc() {
        NodeList nodes = new XpathTool(getNode()).applyXpathForNodeSet("/tei:TEI/tei:teiHeader//tei:sourceDesc");
        if (nodes.getLength() > 0)
            return nodes.item(0);
        else
            return null;
    }

    public TeiDiv getOpus() {
        if (this.parent == null)
            return this;
        else
            return this.parent.getOpus();
    }

    @Override
    public int compareTo(TeiDiv teiDiv) {
        return this.getHead().compareTo(teiDiv.getHead());
    }
}

