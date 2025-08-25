package com.la.dataservices.adesa_rti;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProps {
    private String headerName = "X-Webhook-Secret";
    private String secret;
}
