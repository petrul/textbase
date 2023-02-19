package ro.editii.scriptorium;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/version.properties")
@Getter
public class VersionProperties  {

    public static final String APP_VERSION = "app.version";
//    public static final String BUILD_NUMBER = "build.number";
//    public static final String BUILDDATE = "builddate";
//    public static final String GIT_LATEST_COMMIT = "git_latest_commit";
    public static final String BUILD_MACHINE = "build_machine";

    @Autowired
    public Environment environment;

    @Value("${app.version:}")
    String appVersion;

    @Value("${build.number:}")
    String buildNumber;

    @Value("${build.date:}")
    String buildDate;

    @Value("${git.latest.commit:}")
    String gitLatestCommit;

    @Value("${git.branch:}")
    String gitBranch;

    @Value("${build.machine:}")
    String buildMachine;

    public String getVersion() {
        try {
            return this.environment.getProperty(APP_VERSION);
        } catch (IllegalArgumentException e) {
            return "undefined";
        }
    }

    public String getEnvironmentProperty(String key) {
        try {
            return this.environment.getProperty(key);
        } catch (IllegalArgumentException e) {
            return "undefined";
        }
    }
}
