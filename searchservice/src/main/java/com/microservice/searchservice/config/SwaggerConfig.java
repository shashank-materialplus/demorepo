package com.microservice.searchservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Search Microservice API")
                        .version("1.0.0")
                        .description("Microservice for search functionality using Coveo AI with Adapter Pattern")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@company.com")));
    }
}