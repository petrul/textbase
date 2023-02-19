package ro.editii.scriptorium.tei;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface TeiRepo {
    String getName();
    InputStream getStreamForName(String resName);
    boolean has(String resName);
    File getFile(String resName);
    List<String> list();

    static String PROP_KEY_FILTER = "filter";

}