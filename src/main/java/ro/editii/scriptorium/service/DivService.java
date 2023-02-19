package ro.editii.scriptorium.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.editii.scriptorium.dao.TeiDivRepository;
import ro.editii.scriptorium.model.Author;
import ro.editii.scriptorium.model.TeiDiv;

import java.util.List;

@Service
public class DivService {

    @Autowired
    TeiDivRepository teiDivRepository;


    public List<TeiDiv> getOpera(String authorStrid) {
        return this.teiDivRepository.findOperaForAuthorStrId(authorStrid);
    }

    public List<TeiDiv> getOpera(Author author) {
        return this.getOpera(author.getStrId());
    }
}
