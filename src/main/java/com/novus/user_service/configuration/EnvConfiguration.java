package com.novus.user_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "supmap.properties")
public class EnvConfiguration {
    private String appEmail;
    private String databaseName;
    private String elasticsearchPassword;
    private String elasticsearchUrl;
    private String elasticsearchUsername;
    private String kafkaBootstrapServers;
    private String mailPassword;
    private String mongoUri;
    private String mailRegisterConfirmationLink;
    private String jwtSecret;
    private String mailModifiedUsername;
}
