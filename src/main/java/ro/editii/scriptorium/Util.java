package ro.editii.scriptorium;

import com.ibm.icu.text.Transliterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import ro.editii.scriptorium.model.Author;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 */
public class Util {

    /**
     * windows 11-resistent way to get the file path of a URL representing a file
     */
    public static String urlToFileString(URL url) {
        try {
            final URI uri = url.toURI();
            return Paths.get(uri).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] base64Decode(String s) {
        String str = s.trim()
            .replaceAll("\n", "")
            .replaceAll("\r", "")
            .replaceAll(" ", "")
            .replaceAll("\t", "");
        return Base64.getDecoder().decode(str);
    }

    public static String replaceTilde(String s) {
        return s.replaceFirst("^~", System.getProperty("user.home"));
    }

    // "Nicolae Bălcescu" => nicolae_balcescu
    public static String urlFriendify(String s) {
        assert s != null;

        s = StringUtils.stripAccents(s);

        s = transliterateCyrillic(s);

        String res = s
                .trim()
                .toLowerCase()
                .replaceAll("ș", "s")
                .replaceAll("ş", "s")
                .replaceAll("ț", "t")
                .replaceAll("ţ", "t")
                .replaceAll("ă", "a")
                .replaceAll("î", "i")
                .replaceAll("â", "a")
                .replaceAll("ŭ", "u")
                .replaceAll("à", "a")
                .replaceAll("ü", "u")
                .replaceAll("ö", "o")
                .replaceAll("ô", "o")
                .replaceAll("é", "e")
                .replaceAll("è", "e")
                .replaceAll("ê", "e")
                .replaceAll("ë", "e")
                .replaceAll("ç", "c")
                .replaceAll("ñ", "n")
                .replaceAll("ò", "o")
                .replaceAll("æ", "ae")
                .replaceAll("œ", "oe")
                .replaceAll("ß", "ss")
                .replaceAll("ª", "a")
        ;


        res = res
                .replaceAll("\\.+$", "") // remove final dots
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll("\\*", "")
                .replaceAll("[§¡!?¿’`«»<>'ʹ=…]", "")
                .replaceAll("½", "1_2")
                .replaceAll("¾", "3_4")
                .replaceAll(":", "_")
                .replaceAll("—", "_")
                .replaceAll("-", "_")
                .replaceAll("–", "_") // \u8211
                .replaceAll("\u00A0", "") // remove non-breakable space 0x00A0
        ;

        res  = res
                .replaceAll("[,\\.]", "_")
                .replaceAll("&", "_")
                .replaceAll("[\\?\\!\\[\\]\\\"‘'„”]", "")
                .replaceAll("\\p{M}", "") // remove accents

                .trim() // again, to remove trailing spaces
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")

            ;

        return res;
    }

    public static String transliterateCyrillic(String s) {
        if (containsCyrillic(s)) {
//            String CYRILLIC_TO_LATIN = "Latin-Russian/BGN";
            String CYRILLIC_TO_LATIN = "Russian-Latin/BGN";
            Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
            s = toLatinTrans.transliterate(s);
        }
        return s;
    }

    public static boolean containsCyrillic(String s) {
        return s.chars()
                .mapToObj(Character.UnicodeBlock::of)
                .anyMatch(Character.UnicodeBlock.CYRILLIC::equals);
    }

    public static String randomAlphanumeric(int n) {

        // chose a Character random from this String
        final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    public static String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    // ~/.etext-store
    public static String getAppDotDir() {
        return new File(System.getProperty("user.home"), ".textbase").getAbsolutePath();
    }

    public static Properties readPropertiesFile(InputStream inputStream) {
        final Properties props = new Properties();
        try {
            props.load(new InputStreamReader(inputStream, "UTF-8"));
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<String> readTextFileWithComments(InputStream inputStream) {
        final HashSet<String> result = new HashSet<String>();
        try {
            LineNumberReader reader = new LineNumberReader(
                new InputStreamReader(new BufferedInputStream(inputStream), StandardCharsets.UTF_8)
            );

            String crtLine;
            while ((crtLine = reader.readLine()) != null) {
                String trimmed = crtLine.trim();
                if (trimmed.startsWith("#"))
                    continue;
                result.add(trimmed);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties readPropertiesFileWithComments(InputStream inputStream) {
        final Set<String> strings = readTextFileWithComments(inputStream);
        Set<String[]> splits = strings.stream()
                .map(s -> s.split("="))
                .collect(Collectors.toSet());
        Properties result = new Properties();
        for (String[] s:  splits) {
            result.put(s[0], s[1]);
        }
        return result;
    }

    public static Map<String, Author> readSpecialAuthorsResource() {
        final String resourceName = "special-authors.properties";
        InputStream resourceAsStream = Author.class.getClassLoader().getResourceAsStream(resourceName);
        Properties properties = readPropertiesFileWithComments(resourceAsStream);
        Map<String, Author> result = new HashMap<>();
        for ( String key : properties.stringPropertyNames()) {
            final String value = (String) properties.get(key);
            String[] elems = value.split(";");
            if (elems.length != 4)
                throw new RuntimeException(String.format("a line in file %s should have precisely 4 elements", resourceName));

            int i = 0;

            final String strId          = elems[i++].trim();
            final String firstName      = elems[i++].trim();
            final String lastName       = elems[i++].trim();
            final String displayName    = elems[i++].trim();

            final Author author = Author.builder()
                    .originalNameInTeiFile(displayName)
                    .strId(strId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .displayName(displayName)
                    .build();

            result.put(displayName, author);
        }
        return result;
    }

    public static Set<String> readForbiddenAuthorNames() {
        InputStream resourceAsStream = Author.class.getClassLoader().getResourceAsStream("forbidden-author-names.txt");
        Set<String> strings = readTextFileWithComments(resourceAsStream);
        try {
            resourceAsStream.close();
            return strings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Properties readRecommendedAuthorMappings() {
        return readPropertiesFile(Author.class.getClassLoader().getResourceAsStream("recommended-author-urls.properties"));
    }

    public static Properties asProps(Map<String, String> map) {
        final Properties res = new Properties();
        map.forEach((k, v) -> {
            res.setProperty(k, v);
        });
        return res;
    }

    public static UriComponentsBuilder cloneUriComponentBuilder(UriComponentsBuilder uriComponentsBuilder, HttpServletRequest httpServletRequest) {
        final String xforwardedProto = httpServletRequest != null ? httpServletRequest.getHeader("x-forwarded-proto") : null;
        final String scheme = xforwardedProto != null ? xforwardedProto : httpServletRequest.getScheme();

        UriComponentsBuilder clonedBuilder = uriComponentsBuilder
                .cloneBuilder()
                .scheme(scheme); // works for https too

        return clonedBuilder;
    }

}

class DespartitorInSilabe {
    List<Integer>  desparte (String cuvant) {
        if (cuvant.length() < 3)
            return Collections.emptyList();
            
    throw new RuntimeException();
    }

}

