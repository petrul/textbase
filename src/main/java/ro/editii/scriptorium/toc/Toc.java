package ro.editii.scriptorium.toc;

import ro.editii.scriptorium.model.TeiDiv;

import java.util.*;

public class Toc implements Iterable<TeiDiv> {

    List<TeiDiv> list_divs;
    Map<TeiDiv, Integer> div_indices = new HashMap<>();

    public Toc(TeiDiv div) {

        TeiDiv crt = div;
        List list = new LinkedList();

        parcurge_rec(crt, list);
        this.list_divs = list;

    }


    // parcuregere in depth a tree-ului div
    void parcurge_rec(TeiDiv div, List<TeiDiv> buffer) {
        if (div == null)
            return;

        div_indices.put(div, buffer.size());
        buffer.add(div);

        List<TeiDiv> children = div.getChildren();
        if (children == null)
            return;

        for (TeiDiv c : children) {
            parcurge_rec(c, buffer);
        }
    }

    @Override
    public Iterator<TeiDiv> iterator() {
        return this.list_divs.iterator();
    }

    public TeiDiv prev(TeiDiv div) {
        Integer index = this.div_indices.get(div);
        if (index == null)
            throw new IllegalArgumentException("this toc does not contain div " + div);

        if (index == 0)
            return null;

        return this.list_divs.get(index - 1);
    }

    public TeiDiv next(TeiDiv div) {
        Integer index = this.div_indices.get(div);
        if (index == null)
            throw new IllegalArgumentException("this toc does not contain div " + div);

        if (index >= this.list_divs.size() - 1)
            return null;

        return this.list_divs.get(index + 1);
    }
}
