package ro.editii.scriptorium.toc;

import ro.editii.scriptorium.model.TeiDiv;

import java.util.Iterator;

public class TocIterator implements Iterator<TeiDiv> {

    TeiDiv root;

    public TocIterator(TeiDiv div) {
        this.root = div;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public TeiDiv next() {
        return null;
    }
}
