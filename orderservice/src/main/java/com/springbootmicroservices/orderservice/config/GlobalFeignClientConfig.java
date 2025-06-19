package com.springbootmicroservices.orderservice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class GlobalFeignClientConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_TYPE = "Bearer";

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // This logic is robust and tries to get the token from the SecurityContext first
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                log.debug("GlobalFeignClientConfig: Found JWT in SecurityContext. Propagating token.");
                requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", TOKEN_TYPE, jwt.getTokenValue()));
                return; // Exit after successfully adding the header
            }

            // Fallback to getting header from the raw HttpServletRequest if SecurityContext is empty
            // This can sometimes help in edge cases with thread locals.
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                var request = ((ServletRequestAttributes) requestAttributes).getRequest();
                String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
                if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_TYPE)) {
                    log.debug("GlobalFeignClientConfig: Found JWT in HttpServletRequest header. Propagating token.");
                    requestTemplate.header(AUTHORIZATION_HEADER, authorizationHeader);
                    return;
                }
            }

            log.warn("GlobalFeignClientConfig: Could not find a JWT in SecurityContext or HttpServletRequest to propagate.");
        };
    }
}