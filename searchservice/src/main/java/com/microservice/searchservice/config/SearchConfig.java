package com.microservice.searchservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.Data;

@Configuration
public class SearchConfig {

    @ConfigurationProperties(prefix = "coveo")
    @Data
    public static class CoveoProperties {
        private String searchApiKey;
        private String pushApiKey;
        private String searchEndpoint;
        private String pushApiEndpoint;
        private String searchHub;
        private String sourceId;
        private String organizationId;
        private int timeout = 30000;
        private int maxRetries = 3;
    }

    @Bean
    @ConfigurationProperties(prefix = "coveo")
    public CoveoProperties coveoProperties() {
        return new CoveoProperties();
    }
}