package com.la.dataservices.adesa_rti;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("prod")
public class ProdStagingDataSourceConfig {

    @Bean
    @ConfigurationProperties("staging.datasource")
    @Qualifier("stagingDSProps")
    DataSourceProperties stagingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "stagingDataSource")
    DataSource stagingDataSource(@Qualifier("stagingDSProps") DataSourceProperties props) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "stagingJdbcTemplate")
    JdbcTemplate stagingJdbcTemplate(@Qualifier("stagingDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
