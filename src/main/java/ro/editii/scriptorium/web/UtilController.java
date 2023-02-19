package ro.editii.scriptorium.web;

import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.DebugUtil;
import ro.editii.scriptorium.Util;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Log
@Controller
@RequestMapping("/util")
public class UtilController {

    @Autowired
    TeiDivRepository teiDivRepository;

    @Autowired
    EntityManager entityManager;

    @GetMapping("/echo")
    @ResponseBody
    public ResponseEntity<String> echo(HttpServletRequest request, UriComponentsBuilder uriComponentsBuilder) {
        String respText = DebugUtil.logHttpRequestHeaders(request, uriComponentsBuilder);
        return ResponseEntity.ok().body(respText);
    }

    @GetMapping("/random")
    @ResponseBody
    public ResponseEntity<String> random(HttpServletRequest request, UriComponentsBuilder uriComponentsBuilder) {
        int nr_divs = this.teiDivRepository.getNrOfBottomDivs();
        int rnd_value = new Random().nextInt(nr_divs);

        TypedQuery<TeiDiv> query = this.entityManager.createQuery(
                "select parent from TeiDiv parent left outer join parent.children c where c is null",
                TeiDiv.class);

        query.setFirstResult(rnd_value);
        query.setMaxResults(1);
        TeiDiv singleResult = query.getSingleResult();

        List<String> urlFragments = new ArrayList<>(10);
        TeiDiv div = singleResult;
        while (div != null) {
            LOG.info(div.toString());
            urlFragments.add(div.getUrlFragment());
            div = div.getParent();
        }

        Author author = singleResult.getTeiFile().getAuthor();
        urlFragments.add(author.getStrId());

        Collections.reverse(urlFragments);
        String redirect_to = urlFragments.stream().collect(Collectors.joining("/"));
        redirect_to.replaceAll("\\/\\/", "\\/");

        LOG.info("will redirect to row #{} of a total of {} : got tei id #{} head {} url {}",
                rnd_value, nr_divs, singleResult.getId(), singleResult.getHead(), redirect_to);

        //
        ServerHttpRequest shr = new ServletServerHttpRequest(request);

        final UriComponentsBuilder ucb = Util.cloneUriComponentBuilder(uriComponentsBuilder, request);
        String url = ucb
                .path(redirect_to)
                .build()
                .toUriString();
        ResponseEntity<String> resp = ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
        return resp;
    }
    
    final private static Logger LOG = LoggerFactory.getLogger(UtilController.class);
}
