package ro.editii.scriptorium.rest;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.Util;
import ro.editii.scriptorium.dao.AuthorRepository;
import ro.editii.scriptorium.dto.AuthorDto;
import ro.editii.scriptorium.dto.OpusDto;
import ro.editii.scriptorium.dto.TeiDivDto;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;
import ro.editii.scriptorium.service.DivService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping("/api")
public class RepoRestController {

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    DivService divService;

    @GetMapping("/authors/")
    public List<AuthorDto> getAuthors(UriComponentsBuilder uriComponentsBuilder, HttpServletRequest httpServletRequest) {
        log.info("/authors/");
        List<AuthorDto> authors = this.authorRepository.findAll().stream()
                .sorted()
                .map(it -> AuthorDto.from(it))
                .collect(Collectors.toList());

        // add image if existing
        authors.forEach(it -> {
            it.setImage_href(this.getAuthorThumb(it.getStrId(), uriComponentsBuilder, httpServletRequest));
        });
        return authors;
    }

    // sorry about this, cannot distinguish between the prod proxy and the dev ionic proxy
    String devOrProd(String path, UriComponentsBuilder uriComponentsBuilder) {
        if (!path.startsWith("/"))
            path = "/" + path;
        if (Arrays.stream(this.environment.getActiveProfiles())
                .anyMatch(it -> it.equals("dev"))) { // sorry
            return  "http://localhost:8080" + path;
        } else
            return uriComponentsBuilder.path(path).build().toUriString();
    }

    String getResource(String strid, UriComponentsBuilder uriComponentsBuilder, HttpServletRequest httpServletRequest) {
        final String resName = strid;
        final ClassPathResource resource = new ClassPathResource("/static" + resName);
        if (resource.exists()) {
            UriComponentsBuilder ucb = Util.cloneUriComponentBuilder(uriComponentsBuilder, httpServletRequest);
            return this.devOrProd(resName, ucb);
        }
        return null;
    }

    String getAuthorThumb(String strid, UriComponentsBuilder uriComponentsBuilder, HttpServletRequest httpServletRequest) {
        final String resName = String.format("/img/authors/thumbs/200px/%s.webp", strid);
        return this.getResource(resName, uriComponentsBuilder, httpServletRequest);
    }
    String getAuthorImage(String strid, UriComponentsBuilder uriComponentsBuilder, HttpServletRequest httpServletRequest) {
        final String resName = String.format("/img/authors/%s.jpg", strid);
        return this.getResource(resName, uriComponentsBuilder, httpServletRequest);
    }
    @Autowired
    Environment environment;

    @GetMapping("/authors/{strId}")
    public AuthorDto getAuthor(@PathVariable String strId, UriComponentsBuilder uriComponentsBuilder, HttpServletRequest httpServletRequest) {

        Optional<Author> opt = this.authorRepository.getByStrId(strId);
        if (opt.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        final AuthorDto authorDto = AuthorDto.from(opt.get());
        List<TeiDiv> opera = this.divService.getOpera(authorDto.getStrId());
        List<TeiDivDto> operadto = opera.stream().map(it -> {
            UriComponentsBuilder ucb = uriComponentsBuilder.cloneBuilder();
            final OpusDto opus = OpusDto.from(it);
            opus.setUrl(this.devOrProd(it.getCompletePath(), ucb));
            return opus;
        }).collect(Collectors.toList());
        authorDto.setOpera(operadto.toArray(new OpusDto[0]));
        authorDto.setImage_href(this.getAuthorImage(authorDto.getStrId(), uriComponentsBuilder, httpServletRequest));

        return authorDto;

    }
}
