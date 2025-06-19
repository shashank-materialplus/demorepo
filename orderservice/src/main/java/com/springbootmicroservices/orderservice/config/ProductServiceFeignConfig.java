package com.springbootmicroservices.orderservice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

// IMPORTANT: Do NOT annotate this class with @Configuration
// if you want it to be specific to one Feign client. If it's a general
// config, @Configuration is fine, but let's make it specific to avoid conflicts.
// We will reference it directly from the @FeignClient annotation.
@Slf4j
public class ProductServiceFeignConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_TYPE = "Bearer";

    // This is not a global bean, it's part of this specific configuration class.
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                log.debug("ProductServiceFeignConfig: Adding Bearer token to outgoing Feign request.");
                requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", TOKEN_TYPE, jwt.getTokenValue()));
            } else {
                log.warn("ProductServiceFeignConfig: No JWT in SecurityContext to propagate.");
            }
        };
    }
}