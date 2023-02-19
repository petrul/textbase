package ro.editii.scriptorium.web;


import editii.commons.xml.DomTool;
import editii.commons.xml.XpathTool;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ro.editii.scriptorium.tei.TeiRepo;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/api/files")
public class FilesController {

    @Autowired
    TeiRepo teiRepo;

    @GetMapping("/")
    public @ResponseBody List<String> getFiles() {
        return teiRepo.list();
    }

    /**
     * serves the whole file
     * @param response
     */
    @GetMapping("/file")
    public void getFile(@RequestParam String name , HttpServletRequest request, HttpServletResponse response) {
        try {
//            String urlStart = "/api/files/";
//
//            final String _path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//            LOG.info("PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE : " + _path);
//
//            if (! _path.startsWith(urlStart))
//                throw new RuntimeException("url should start with " + urlStart);

//            String filename = _path.substring(urlStart.length());

            response.setHeader("Content-type", "application/xml; charset=utf-8");
            String filename = name;
            filename = filename.replaceAll("\\/+","\\/");

            LOG.info(String.format("serving file [%s] ", filename));
            InputStream is = this.teiRepo.getStreamForName(filename);

            ServletOutputStream os = response.getOutputStream();
            IOUtils.copy(is, os);
            os.flush();
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(String.format("file [%s] not found", name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * serves a fragment of a file
     * @param file
     * @param request
     * @param response
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
//    @GetMapping("/{file}/**")
    public void getFragment(@PathVariable String file,
                            HttpServletRequest request,
                            HttpServletResponse response)
            throws
            IOException, SAXException, XPathExpressionException {

        String urlStart = "/api/files/" + file;

        LOG.info(String.format("fragments from [%s] for xpath : [%s]", file, "xpath"));

        final String _path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOG.info("PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE : " + _path);

        if (! _path.startsWith(urlStart))
            throw new RuntimeException("url should start with " + urlStart);
        String xpath = _path.substring(urlStart.length());

        LOG.info("xpath : " + xpath);

        File sourceFile = this.teiRepo.getFile(file);
        LOG.info("" + sourceFile);

        XpathTool xt = new XpathTool(sourceFile);
        NodeList nodeList = xt.applyXpathForNodeSet(xpath);

        Node grouped = DomTool.rootForResults(nodeList);

        DomTool.serialize(grouped, response.getOutputStream());

    }



    static Logger LOG = LoggerFactory.getLogger(FilesController.class);
}
