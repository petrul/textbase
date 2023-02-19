package ro.editii.scriptorium.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.dto.AuthorsAndTeiDivsDto;
import ro.editii.scriptorium.service.AuthorsAndTeiDivs;
import ro.editii.scriptorium.service.SearchService;

import javax.validation.constraints.Size;

@RestController
@RequestMapping("/api/search")
public class SearchRestController {
    @Autowired
    TeiDivRepository teiDivRepository;

    @Autowired
    SearchService searchService;

    @GetMapping("")
    @ResponseBody
    public AuthorsAndTeiDivsDto search(@RequestParam @Size(min = 3) String q, UriComponentsBuilder uriComponentsBuilder) {
        AuthorsAndTeiDivs authorsAndTeiDivs= this.searchService.searchAuthorsAndWorks(q);
        return AuthorsAndTeiDivsDto.from(authorsAndTeiDivs, uriComponentsBuilder);
    }
}
