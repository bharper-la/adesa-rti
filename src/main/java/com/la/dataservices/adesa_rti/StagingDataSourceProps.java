// src/main/java/com/la/dataservices/adesa_rti/StagingDataSourceProps.java
package com.la.dataservices.adesa_rti;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "staging.datasource")
public class StagingDataSourceProps {
    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    // optional Hikari settings
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private String poolName;
}
