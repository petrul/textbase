package ro.editii.scriptorium.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.dto.TeiDivDto;
import ro.editii.scriptorium.model.TeiDiv;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/div")
public class DivRestController {

    @Autowired
    TeiDivRepository teiDivRepository;

    @GetMapping("/{id}")
    public @ResponseBody TeiDivDto getTeiDiv(@PathVariable long id, UriComponentsBuilder uriComponentsBuilder) {
        TeiDiv teiDiv = this.teiDivRepository.getOne(id);
        String baseUrl = uriComponentsBuilder.path("/").toUriString();
        TeiDivDto teiDivDto = TeiDivDto.fromTeiDiv(teiDiv, baseUrl);
        return teiDivDto;
    }

    @GetMapping("/")
    public List<TeiDivDto> getTeiDivs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            UriComponentsBuilder uriComponentsBuilder) {
        Pageable pageRequest = PageRequest.of(page, size);
        Page<TeiDiv> teiDivs = this.teiDivRepository.findAll(pageRequest);

        String baseUrl = uriComponentsBuilder.path("/").toUriString();
        LOG.debug("baseurl {}" , baseUrl);
        List<TeiDivDto> dtoList = teiDivs.stream()
                .map(it -> TeiDivDto.fromTeiDiv(it, baseUrl))
                .collect(Collectors.toList());

        return dtoList;
    }



    final private static Logger LOG = LoggerFactory.getLogger(DivRestController.class);
}