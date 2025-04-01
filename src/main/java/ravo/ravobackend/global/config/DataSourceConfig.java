package ravo.ravobackend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // liveDataSource 를 default dataSource로 등록
    @Bean(name = {"dataSource", "liveDataSource"})
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.live")
    public DataSource liveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "standbyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.standby")
    public DataSource standbyDataSource() {
        return DataSourceBuilder.create().build();
    }


}
