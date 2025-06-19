package com.springbootmicroservices.productservice.config;

import com.springbootmicroservices.productservice.filter.CustomBearerTokenAuthenticationFilter;
import com.springbootmicroservices.productservice.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration class named {@link SecurityConfig} for setting up security filters and policies.
 * Configures HTTP security settings, CORS, CSRF, and session management.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Ensures @PreAuthorize on controller methods still works where applied
@Slf4j
public class SecurityConfig {

    /**
     * Configures the session authentication strategy.
     *
     * @return a {@link SessionAuthenticationStrategy} instance
     */
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * @param httpSecurity the {@link HttpSecurity} to configure
     * @param customBearerTokenAuthenticationFilter the custom filter for token authentication
     * @param customAuthenticationEntryPoint the custom entry point for authentication errors
     * @return a configured {@link SecurityFilterChain} instance
     * @throws Exception if an error occurs while configuring security
     */
    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity httpSecurity,
            final CustomBearerTokenAuthenticationFilter customBearerTokenAuthenticationFilter,
            final CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) throws Exception {

        log.debug("Configuring Security Filter Chain");

        httpSecurity
                .exceptionHandling(customizer -> customizer.authenticationEntryPoint(customAuthenticationEntryPoint))
                .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to GET product endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/products").permitAll() // For getProducts (list with paging)
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/{productId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/category/{category}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/{productId}/purchase").permitAll()


                        // Secure mutating endpoints (POST, PUT, DELETE) - these will use @PreAuthorize from controller
                        // Or you can define authorities here as well, e.g.:
                        // .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAuthority("ADMIN")
                        // .requestMatchers(HttpMethod.PUT, "/api/v1/products/{productId}").hasAuthority("ADMIN")
                        // .requestMatchers(HttpMethod.DELETE, "/api/v1/products/{productId}").hasAuthority("ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(customBearerTokenAuthenticationFilter, BearerTokenAuthenticationFilter.class);

        log.debug("CustomBearerTokenAuthenticationFilter added to the filter chain");

        return httpSecurity.build();
    }

    /**
     * Provides CORS configuration for the application.
     *
     * @return a {@link CorsConfigurationSource} instance
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Allows all origins
        // For production, you might want to restrict this to your frontend's URL:
        // configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://yourfrontend.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // Explicitly list common methods
        configuration.setAllowedHeaders(List.of("*")); // Allows all headers
        // configuration.setAllowCredentials(true); // Uncomment if you need to send cookies or use session-based auth with CORS
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply this CORS configuration to all paths
        return source;
    }

    /**
     * Provides a {@link PasswordEncoder} bean for encoding passwords.
     *
     * @return a {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}