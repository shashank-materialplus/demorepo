package com.springbootmicroservices.orderservice.config;

import com.springbootmicroservices.orderservice.filter.CustomBearerTokenAuthenticationFilter;
import com.springbootmicroservices.orderservice.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // For @PreAuthorize etc.
public class SecurityConfig {

    private final CustomBearerTokenAuthenticationFilter customBearerTokenAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .exceptionHandling(customizer -> customizer.authenticationEntryPoint(customAuthenticationEntryPoint))
                .cors(AbstractHttpConfigurer::disable) // Disable CORS here, API Gateway should handle it
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Example: Stripe webhook might be public or require specific auth not covered by user JWTs
                        // .requestMatchers(HttpMethod.POST, "/api/v1/payments/stripe/webhook").permitAll()

                        // Order placement and history require an authenticated user
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/history").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/{orderId}").authenticated() // User can see their own order

                        // Payment processing requires an authenticated user
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/process").authenticated()

                        // Updating order status is an admin-only operation
                        .requestMatchers(HttpMethod.PUT, "/api/v1/orders/{orderId}/status").hasAuthority("ROLE_ADMIN")
                        //.requestMatchers(HttpMethod.POST, "/api/v1/payments/stripe/webhook").permitAll()

                        // Example for an admin endpoint to view all orders (if you add it)
                        // .requestMatchers(HttpMethod.GET, "/api/v1/orders/admin/all").hasAuthority("ADMIN")


                        .anyRequest().authenticated() // By default, all other requests require authentication
                )
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(customBearerTokenAuthenticationFilter, BearerTokenAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Not directly used by OrderService for user passwords, but good to have for consistency
        // if any service-level hashing were ever needed.
        return new BCryptPasswordEncoder();
    }
}