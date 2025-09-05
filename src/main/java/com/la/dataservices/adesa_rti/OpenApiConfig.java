package com.la.dataservices.adesa_rti;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final BuildProperties buildProperties;
    @Value("${git.commit.id.abbrev:unknown}")
    private String commitId;

    public OpenApiConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI adesaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                    .title("Adesa RTI API")
                    .version(commitId)
                    .description("RTI integration endpoints\n\n Build time: " + buildProperties.getTime())
                    .contact(new Contact()
                            .name("Data Services")
                            .email("bradharper@laserappraiser.com")
                            .url("https://laserappraiser.com")))
            .components(new Components()
                    .addSecuritySchemes("webhookApiKey",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.APIKEY)
                                    .in(SecurityScheme.In.HEADER)
                                    .name("X-Webhook-API-Key")
                                    .description("API key for webhook authentication"))
                    .addSecuritySchemes("webhookSecret",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.APIKEY)
                                    .in(SecurityScheme.In.HEADER)
                                    .name("X-Webhook-Secret")
                                    .description("Shared secret for webhook verification")))
            .addSecurityItem(new SecurityRequirement()
                    .addList("webhookApiKey")
                    .addList("webhookSecret"));
    }
}

