package ravo.ravobackend.global;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.batch")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public JdbcTransactionManager batchTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.active")
    public DataSourceProperties activeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "activeDataSource")
    public DataSource activeDataSource(
            @Qualifier("activeDataSourceProperties") DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.standby")
    public DataSourceProperties standbyDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "standbyDataSource")
    public DataSource standbyDataSource(
            @Qualifier("standbyDataSourceProperties") DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder().build();
    }

    @Bean(name = "activeJdbcTemplate")
    public JdbcTemplate activeJdbcTemplate(
            @Qualifier("activeDataSource") DataSource ds
    ) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "standbyJdbcTemplate")
    public JdbcTemplate standbyJdbcTemplate(
            @Qualifier("standbyDataSource") DataSource ds
    ) {
        return new JdbcTemplate(ds);
    }
}
