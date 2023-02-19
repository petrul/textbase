package editii.commons.xml;

import java.util.HashMap;
import java.util.Map;

public class TeiNamespaceResolver extends MapNamespaceResolver {

    public static String TEI_NS = "http://www.tei-c.org/ns/1.0";
    public static String XML_NS = "http://www.w3.org/XML/1998/namespace";

    public TeiNamespaceResolver() {
        super(null);
        Map<String, String> map = new HashMap<>();
        map.put("tei", TEI_NS);
        map.put("xml", XML_NS);
        super.setMap(map);
    }

}
