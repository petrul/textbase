package editii.commons.xml;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapNamespaceResolver implements NamespaceContext {

    public void setMap(Map<String, String> map) {
        this.map = map;
        this.reversedMap = new HashMap<>();
        if (map != null)
            map.forEach( (k,v) -> {
                this.reversedMap.put(v, k);
            });

    }

    protected Map<String, String> map;
    protected Map<String, String> reversedMap;

    public MapNamespaceResolver(Map<String, String> map) {
        this.setMap(map);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return this.map.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return this.reversedMap.get(namespaceURI);
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        throw new RuntimeException("unimplemented");
    }
}
