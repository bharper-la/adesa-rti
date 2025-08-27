// src/main/java/com/la/dataservices/adesa_rti/ProdStagingDataSourceConfig.java
package com.la.dataservices.adesa_rti;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("prod") // only create these when prod is active
@EnableConfigurationProperties(StagingDataSourceProps.class)
@RequiredArgsConstructor
public class ProdStagingDataSourceConfig {

    private final StagingDataSourceProps props;

    @Bean(name = "stagingDataSource")
    public DataSource stagingDataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getUrl());
        cfg.setUsername(props.getUsername());
        cfg.setPassword(props.getPassword());
        cfg.setDriverClassName(props.getDriverClassName());
        if (props.getMaximumPoolSize() != null) cfg.setMaximumPoolSize(props.getMaximumPoolSize());
        if (props.getMinimumIdle() != null) cfg.setMinimumIdle(props.getMinimumIdle());
        if (props.getPoolName() != null) cfg.setPoolName(props.getPoolName());
        return new HikariDataSource(cfg);
    }

    @Bean(name = "stagingJdbcTemplate")
    public JdbcTemplate stagingJdbcTemplate(@Qualifier("stagingDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
