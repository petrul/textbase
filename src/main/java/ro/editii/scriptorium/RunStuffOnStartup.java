package ro.editii.scriptorium;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class RunStuffOnStartup {

    @Bean
    public CommandLineRunner printJdbcUrlCLR(DataSource dataSource) {
        return args -> {
            try {
                System.out.println("jdbc url: " + dataSource.getConnection().getMetaData().getURL());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
