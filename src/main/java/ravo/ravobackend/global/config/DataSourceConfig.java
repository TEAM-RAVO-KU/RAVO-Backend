package ravo.ravobackend.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // liveDataSource 를 default dataSource로 등록
    @Bean(name = {"dataSource", "activeDataSource"})
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.active")
    public DataSource liveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "standbyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.standby")
    public DataSource standbyDataSource() {
        return DataSourceBuilder.create().build();
    }


    /**
     * standby db로 write 하기 위해 추가
     */
    @Bean
    public JdbcTemplate standbyJdbcTemplate(@Qualifier("standbyDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

}
