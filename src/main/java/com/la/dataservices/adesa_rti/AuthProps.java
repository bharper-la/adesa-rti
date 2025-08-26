package com.la.dataservices.adesa_rti;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProps {
    /** Secret header name (e.g., X-Webhook-Secret) */
    private String headerName = "X-Webhook-Secret";

    /** API key header name (e.g., X-Webhook-API-Key) */
    private String apiKeyHeaderName = "X-Webhook-API-Key";

    /** apiKey -> secret mappings */
    private Map<String, String> clients = new HashMap<>();
}
