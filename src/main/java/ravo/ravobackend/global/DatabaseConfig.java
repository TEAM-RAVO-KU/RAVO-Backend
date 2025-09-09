package ravo.ravobackend.global;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    @Bean
    @ConfigurationProperties(prefix = "application.database.active")
    public DatabaseProperties activeDatabaseProperties() {
        return new DatabaseProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "application.database.standby")
    public DatabaseProperties standbyDatabaseProperties() {
        return new DatabaseProperties();
    }

}
