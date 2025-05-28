package ravo.ravobackend.global;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name = {"dataSource", "activeDataSource"})
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.active")
    public DataSource activeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.standby")
    public DataSource standbyDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.batch")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean
    @BatchDataSource
    public DataSource batchDataSource() {
        return batchDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    /**
     * standby db로 write 하기 위해 추가
     */
    @Bean
    public JdbcTemplate standbyJdbcTemplate(@Qualifier("standbyDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    @Primary
    public JdbcTemplate activeJdbcTemplate(@Qualifier("activeDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
