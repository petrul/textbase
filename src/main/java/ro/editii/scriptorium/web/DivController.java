package ro.editii.scriptorium.web;

import editii.commons.xml.DomTool;
import editii.commons.xml.TeiDocument;
import editii.commons.xml.XpathTool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.editii.scriptorium.Util;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dao.RelocationRepository;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.Relocation;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.model.TeiFile;
import ro.editii.scriptorium.tei.TeiRepo;
import ro.editii.scriptorium.toc.Toc;
import ro.editii.scriptorium.xslt.XsltTool;

import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import javax.xml.transform.Transformer;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * "main" controller: saves bits of xml (works, chapters, divs)
 */
@Log
@Controller
@RequestMapping("/")
public class DivController {

    final static String AUTHOR_REGEX   = "/{authorId:^(?!(?:css|img|js|api|util|search|p|admin|app)$)[a-z0-9\\_]+$}";
    final static String DIVPAGE_START_REGEX  = AUTHOR_REGEX + "/{opusId:^[a-z0-9\\_]+$}";

    final static String DIVPAGE_REGEX  = DIVPAGE_START_REGEX + "/**";

    final static String BIN_OBJ_REGEX  = DIVPAGE_START_REGEX + "/_binary/{id}";
    final static String BIN_OBJ_REGEX2  = DIVPAGE_START_REGEX + "/*/_binary/{id}";
    final static String BIN_OBJ_REGEX3  = DIVPAGE_START_REGEX + "/*/*/_binary/{id}";
    final static String BIN_OBJ_REGEX4  = DIVPAGE_START_REGEX + "/*/*/*/_binary/{id}";

    static Transformer TEIDIV_TO_HTML_XSLT;

    static {
        URL xsltResource = DivController.class.getClassLoader().getResource("xslt/teidiv2html.xsl");
        try {
            TEIDIV_TO_HTML_XSLT = XsltTool.getTransformer(xsltResource.openStream(), "xslt/teidiv2html.xsl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    TeiDivRepository teiDivRepository;

    @Autowired
    RelocationRepository relocationRepository;

    @Autowired
    EntityManager em;

    @Autowired
    TeiRepo teiRepo;


    @GetMapping("/")
    public String index(Model model) {
        LOG.debug("public String index(Model model)");
        List<Author> all = this.authorRepository
                .findAll().stream()
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("authors", all);
        return "index";
    }

    @GetMapping(value = { BIN_OBJ_REGEX, BIN_OBJ_REGEX2, BIN_OBJ_REGEX3, BIN_OBJ_REGEX4})
    public void binaryObject(HttpServletResponse httpServletResponse,
                             @PathVariable String authorId,
                             @PathVariable String opusId,
                             @PathVariable String id ) {
        try {
            final TeiDiv op = this.getOpus(authorId, opusId);
            final Node rootNode = op.getNode();
            TeiDocument tei = new TeiDocument(rootNode);
            byte[] binaryObject = tei.getBinaryObject(id);
            httpServletResponse.addHeader(HttpHeaders.CONTENT_TYPE, "image/jpeg");
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            outputStream.write(binaryObject);
            outputStream.flush();
            outputStream.close();
        } catch (IOException | IllegalArgumentException e) {
            writeErrorToHttpServletResp(httpServletResponse, e, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    protected void writeErrorToHttpServletResp(HttpServletResponse httpServletResponse, Exception exception, int code) {
        httpServletResponse.setStatus(code);
        try {
            httpServletResponse.getWriter().println(exception.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // author page : the author must not be a list of words: css, js etc in order to allow service static content (mainly css and js)
    @GetMapping(AUTHOR_REGEX)
    public ModelAndView author(
            @PathVariable String authorId,
            Model model,
            UriComponentsBuilder uriComponentsBuilder)
    {

        Author author = this.retrieveAuthor(authorId);
        model.addAttribute("author", author);


        List<TeiFile> listTeiFiles = this.em
                .createQuery("select tf from TeiFile tf " +
                        " join tf.authors a " +
                        " where a.strId = :authId", TeiFile.class)
                .setParameter("authId", authorId)
                .getResultList();


        model.addAttribute("listTeiFiles", listTeiFiles);
        for (TeiFile tf : listTeiFiles ) {
            LOG.info(tf.toString());
        }
        LOG.debug(">> tei files: " + listTeiFiles);

        List<TeiDiv> rootDivs = this.teiDivRepository
                .findOperaForAuthorStrId(authorId)
                .stream().sorted().collect(Collectors.toList());

        LOG.debug(">> root divs: " + rootDivs);

        model.addAttribute("rootDivs", rootDivs);

        String thisUrl = uriComponentsBuilder
                .path("/{author}")
                .buildAndExpand(author.getStrId())
                .toUriString();

        model.addAttribute("thisUrl", thisUrl);

        ModelAndView mv = new ModelAndView("author");
        mv.getModel().putAll(model.asMap());
        return mv;
    }



    // div page for browser as html
    @GetMapping(value = DIVPAGE_REGEX, produces = "text/html")
    public String teiDivAsHtml(@PathVariable String authorId,
                               @PathVariable String opusId,
                               HttpServletRequest request,
                               Model model,
                               UriComponentsBuilder uriComponentsBuilder) {

        DivInfo divInfo = this._getTeiDiv(authorId, opusId, model, request, uriComponentsBuilder);
        Node selectedDiv = divInfo.getNode();
        final XpathTool xt = new XpathTool(selectedDiv);
        NodeList subdivs = xt.applyXpathForNodeSet("tei:div");
        if (subdivs.getLength() > 0) {
            for (int i = 0; i < subdivs.getLength(); i++)
                selectedDiv.removeChild(subdivs.item(i));
        }

        String featuredImage = null;
        // a featured image for this div?
        //NodeList anyImages = xt.applyXpathForNodeSet(".//binaryObject");
        List<Node> anyImages = new ArrayList<>();
        xt.visit((Node n) -> {
                    if ("binaryObject".equals(n.getNodeName()))
                        anyImages.add(n);
                        return n;
                });
        if (anyImages.size() > 0) {
            final Node node = anyImages.get(0);// take first image
            final Node idAttrNode = node.getAttributes().getNamedItem("xml:id");
            if (idAttrNode != null) {
                final String imageId = idAttrNode.getNodeValue();
                featuredImage = Util.cloneUriComponentBuilder(uriComponentsBuilder, request)
                        .path(authorId)
                        .path("/" + opusId)
                        .path("/_binary/" + imageId)
                        .toUriString();
            }
        }
        if (featuredImage == null) {
            featuredImage = getRandomImageUrl();
        }
        model.addAttribute("featuredImage", featuredImage);

        // ../../..
        String relativeRoot = IntStream.range(0, divInfo.getTeiDiv().getDepth())
                .mapToObj(it -> "..")
                .collect(Collectors.joining("/"));
        if (relativeRoot.length() > 0) relativeRoot = "/" + relativeRoot;

        String res = XsltTool.apply(TEIDIV_TO_HTML_XSLT, selectedDiv,
                Map.of(
                "requestURI", request.getRequestURI(),
                "author", divInfo.getAuthor().getVisualName(),
                "relativeRoot", relativeRoot
                ));

        LOG.debug("text size:" + res.length());
        model.addAttribute("text", res);

        return "teidiv";

    }

    private String getRandomImageUrl() {
        final List<String> urls = imageUrls();
        final int i = new Random().nextInt(urls.size());
        return urls.get(i);
    }

    // raw div as tei xml
    // @GetMapping(value = DIVPAGE_REGEX, produces = "application/xml")
    public void teiDivAsXml(@PathVariable String authorId,
                            @PathVariable String opusId,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            Model model,
                            UriComponentsBuilder uriComponentsBuilder) throws IOException {
        Node selectedDiv = this._getTeiDiv(authorId, opusId, model, request, uriComponentsBuilder).getNode();

        DomTool.serialize(selectedDiv, response.getOutputStream());
    }

    protected Author retrieveAuthor(String authorStrId) {
        Author author;
        List<Author> authors = this.authorRepository.findByStrId(authorStrId);
        if (authors != null && authors.size() == 1) {
            author = authors.get(0);
        } else
            throw new ResourceNotFoundException(String.format("no author for id [%s]", authorStrId));

        return author;
    }


    /**
     *
     */
    protected DivInfo _getTeiDiv(final String authorId, String opusId,
                                 Model model, HttpServletRequest request,
                                 UriComponentsBuilder uriComponentsBuilder) {

        DivInfo.DivInfoBuilder respBuilder = DivInfo.builder();
        // 1. get author /author

        Author author;

        {
            author = this.retrieveAuthor(authorId);

            LOG.debug(">> " + author);
            model.addAttribute("author", author);
            respBuilder.author(author);

        }

        // 2. get root div aka opusId /author/opusId

        TeiDiv op = this.getOpus(authorId, opusId);
        model.addAttribute("op", op);

        TeiDiv div = op;

        // 3. iterative : for each div: div1,div2,div3 from /author/opusId/div1/div2/div3

        {
            final String _path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            LOG.info(_path);

            checkRelocation(_path, uriComponentsBuilder);

            String beginningOfUrl = String.format("/%s/%s", authorId, opusId);
            if (! _path.startsWith(beginningOfUrl))
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("expected url to start with %s", beginningOfUrl));

            String endPartOfUrl = _path.substring(beginningOfUrl.length());
            if (endPartOfUrl.startsWith("/"))
                endPartOfUrl = endPartOfUrl.substring(1);

            if (endPartOfUrl.length() > 0) {
                String[] splits = endPartOfUrl.split("/");
                LOG.debug(String.format("%d splits : %s", splits.length, Arrays.toString(splits)));

                for (String fragm: splits) {
                    if ("".equals(fragm.trim()))
                        continue;

                    List<TeiDiv> children = div.getChildren();

                    List<TeiDiv> filtered = children.stream().filter(it -> fragm.equals(it.getUrlFragment())).collect(Collectors.toList());
                    if (filtered.size() < 1) {
                        // none found by name: maybe fragm is not a fragment name but only its number
                        try {
                            int splitAsInt = Integer.parseInt(fragm);
                            if (splitAsInt >= children.size())
                                throw new RuntimeException(String.format("fragment nr %d is bigger than total number of children %d", splitAsInt, children.size()));

                            TeiDiv child = children.get(splitAsInt);
                            child.setTeiRepo(this.teiRepo);
                            div = child;
                        } catch (NumberFormatException e) {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("no div found named [%s] under [%s]", fragm, beginningOfUrl));
                        }
                    } else if (filtered.size() > 1) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("several (%d) divs found named [%s] under [%s]", filtered.size(), fragm, beginningOfUrl));
                    } else {
                        TeiDiv child = filtered.get(0);
                        child.setTeiRepo(this.teiRepo);
                        div = child;
                    }
                }
            }
        }
        respBuilder.teiDiv(div);

        {
            // 3,5 : get breadcrumb

            List<TeiDiv> breadcrumb = new ArrayList<>();
            TeiDiv divIt = div;
            while (divIt != null) {
                breadcrumb.add(divIt);
                divIt = divIt.getParent();
            }
            Collections.reverse(breadcrumb);
            model.addAttribute("breadcrumb", breadcrumb);
        }

        // 4. get content of identified div and spit it out

        List<TeiDiv> children = div.getChildren();

        Toc toc = new Toc(div.getOpus());
        model.addAttribute("div", div);
        model.addAttribute("prev", toc.prev(div));
        model.addAttribute("next", toc.next(div));
        model.addAttribute("children", children);

        respBuilder.children(children);

        {
            Node selectedDiv = div.getNode(); // nodeList.item(0);

            // 5. language, license and sourceDesc

            String language = div.getLanguage();
            String license = div.getLicense();

            String sourceDesc = "";
            {
                Node node_source_desc = div.getSourceDesc();
                if (node_source_desc !=null )
                    sourceDesc = XsltTool.apply(TEIDIV_TO_HTML_XSLT, node_source_desc, Map.of("baseUrl", request.getRequestURI()));
            }

            if (language != null && !"".equals(language.trim()))
                    model.addAttribute("language", language);

            if (license != null && ! "".equals(license.trim()))
                model.addAttribute("license", license);

            if (sourceDesc != null && ! "".equals(sourceDesc.trim()))
                model.addAttribute("source", sourceDesc);

            final Node deepCopy = DomTool.deepCopy(selectedDiv);
            respBuilder.node(deepCopy);

            return respBuilder.build();
        }
    }

    protected void checkRelocation(String path, UriComponentsBuilder uriComponentsBuilder) {
        Optional<Relocation> byId = this.relocationRepository.findById(path);
        if (byId.isPresent()) {
            throw new RelocationException(uriComponentsBuilder, byId.get().getNewPath());
        }
    }

    private TeiDiv getOpus(String authorId, String opusId) {
        List<TeiDiv> opera = this.teiDivRepository.findOperaForAuthorStrId(authorId);

        opera = opera.stream().filter( teidiv -> teidiv.getUrlFragment().equals(opusId)).collect(Collectors.toList());

        LOG.debug(String.format("%d opera for author %s", opera.size(), opera));
        if (opera.size() < 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("no opus for [%s/%s]", authorId, opusId));

        if (opera.size() > 1)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("several (%d) divs correspond to this url (%s), expected exactly 1", opera.size(), opusId));

        final TeiDiv op = opera.get(0);
        op.setTeiRepo(this.teiRepo);
        return op;
    }


    @GetMapping("/search")
    public String search(@RequestParam @Size(min = 3) String q, Model model) {
        q = q.trim();
        model.addAttribute("q", q);
        
        if (q.length() >= 3) {
            List<Author> lastnames = this.authorRepository.findByLastNameContainingIgnoreCase(q);
            List<Author> firstnames = this.authorRepository.findByFirstNameContainingIgnoreCase(q);
    
            List<Author> authors = new ArrayList<>();
            authors.addAll(lastnames);
            authors.addAll(firstnames);
    
            List<TeiDiv> divs = this.teiDivRepository.findByHeadContainingIgnoreCase(q);
    
            model.addAttribute("authors", authors);
            model.addAttribute("divs", divs);    
        }

        return "search";
    }

    static List<String> IMAGE_URLS = null;
    synchronized static List<String> imageUrls() {
        if (IMAGE_URLS != null)
            return IMAGE_URLS;

        IMAGE_URLS = new ArrayList<>(Util.readTextFileWithComments(DivController.class.getClassLoader().getResourceAsStream("beautiful-images.txt")));
        IMAGE_URLS = IMAGE_URLS.stream().map(String::trim).collect(Collectors.toList());
        return IMAGE_URLS;
    }
    static Logger LOG = LoggerFactory.getLogger(DivController.class);
}

@Data @AllArgsConstructor @NoArgsConstructor @Builder
class DivInfo {
    TeiDiv  teiDiv;
    List<TeiDiv> children;
    Node    node;
    Author  author;
}

@ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
class RelocationException extends ResponseStatusException {

    private final String path;

    UriComponentsBuilder uriComponentsBuilder;
    public RelocationException(UriComponentsBuilder uriComponentsBuilder, String newPath) {
        super(HttpStatus.MOVED_PERMANENTLY);
        this.uriComponentsBuilder = uriComponentsBuilder;
        this.path = newPath;
    }

    @Override
    public HttpHeaders getResponseHeaders() {
        HttpHeaders resp = new HttpHeaders();
        resp.addAll(super.getResponseHeaders());
        resp.set(HttpHeaders.LOCATION, uriComponentsBuilder.path(this.path).toUriString());
        return resp;
    }
}