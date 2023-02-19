package ro.editii.scriptorium.tei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.editii.scriptorium.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TeiDirRepoImpl implements TeiRepo {

    String teiDir;

    Properties properties;



    public String getName() {
        return teiDir;
    }

    public TeiDirRepoImpl(String teiDir, Properties properties) {
        this.teiDir = Util.replaceTilde(teiDir);
        if (properties == null)
            this.properties = new Properties();
        else
            this.properties = properties;

        LOG.info("TEI Repo @ [" + this.teiDir +"]" );
    }

    public TeiDirRepoImpl(String teiDir, Map<String, String> properties) {
        this(teiDir, Util.asProps(properties));
    }

    public TeiDirRepoImpl(String teiDir, String filter) {
        this(teiDir, Util.asProps(Map.of(PROP_KEY_FILTER, filter)));
    }

    public void setProp(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public String getProp(String key) {
        return this.properties.getProperty(key);
    }

    public String getFilter() {
        return this.getProp(PROP_KEY_FILTER);
    }

    public TeiDirRepoImpl(String teiDir) {
        this(teiDir, (Properties) null);
    }

    public File getFile(String resName) {
        return new File(this.getTeiDir(), resName);
    }

    public boolean has(String resName) {
        File file = this.getFile(resName);
        return file.exists();
    }

    public InputStream getStreamForName(String resName) {
        File file = this.getFile(resName);

        BufferedInputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return is;
    }

    public List<String> list() {
        File file = new File(this.getTeiDir());
        if (!file.exists() || !file.isDirectory())
            throw new RuntimeException("file " + this.teiDir + " is not a dir");

        Path teidirPath = Paths.get(this.getTeiDir());

        try {

            final Path finalTeiDirPath;
            if (Files.isSymbolicLink(teidirPath))
                finalTeiDirPath = Paths.get(teidirPath.toRealPath().toString());
            else
                finalTeiDirPath = teidirPath;

            List<String> collect = Files.walk(finalTeiDirPath)
                    .filter(Files::isRegularFile)
                    .map(it -> it.toString())
//                    .filter(it -> !it.contains("orig/"))
                    .filter(it -> it.toLowerCase().endsWith(".xml"))
                    .map(it -> it.substring(finalTeiDirPath.toString().length()))
                    .collect(Collectors.toList());

            String filter = this.getFilter();
            if (filter != null && !filter.trim().isEmpty()) {
                    filter = "(?i)" + filter; // make it case insensitive for free

                Pattern ptrn = Pattern.compile(filter);
                collect = collect.stream()
                        .filter(it -> ptrn.matcher(it).find())
                        .collect(Collectors.toList());
            }

            if (collect.size() < 1) {
                LOG.warn("teirepo " + this.getName() + " is empty");
            }

            return collect;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        return "dir:" + this.teiDir;
    }

    public String getTeiDir() {
        return Util.replaceTilde(this.teiDir);
    }

    static Logger LOG = LoggerFactory.getLogger(TeiDirRepoImpl.class);
}


