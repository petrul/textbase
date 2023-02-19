package ro.editii.scriptorium;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import ro.editii.scriptorium.tei.TeiDirRepoImpl;
import ro.editii.scriptorium.tei.TeiRepo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;

@TestConfiguration
@Import(AppConfig.class)
public class TestConfig {

    @Bean
    public TeiRepo teiRepo() {
        final String dirname = Util.urlToFileString(this.getClass().getClassLoader().getResource("testrepo"));
        return new TeiDirRepoImpl(dirname);
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Bean
    public RestTemplate restTemplateNoRedirect(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory noRedirectFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        return builder
                .setConnectTimeout(Duration.ofSeconds(100 * 60))
                .setReadTimeout(Duration.ofSeconds(100 * 60))
                .requestFactory(() -> noRedirectFactory)
                .build();
    }
}
