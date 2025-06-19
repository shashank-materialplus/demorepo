package com.springbootmicroservices.orderservice.filter;

import com.springbootmicroservices.orderservice.service.TokenService;
import io.jsonwebtoken.JwtException; // JJWT's base exception
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomBearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String jwt = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            jwt = authorizationHeader.substring(TOKEN_PREFIX.length());
        } else {
            log.trace("Authorization header does not begin with Bearer prefix or is missing for URI: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) { // Only process if not already authenticated
            try {
                tokenService.validateToken(jwt); // Basic validation (throws on failure)

                UsernamePasswordAuthenticationToken authenticationToken = tokenService.getAuthentication(jwt);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.debug("User '{}' authenticated successfully via JWT for URI: {}", authenticationToken.getName(), request.getRequestURI());

            } catch (ResponseStatusException e) {
                // TokenService throws ResponseStatusException for validation errors (expired, malformed, etc.)
                // These will be handled by the AuthenticationEntryPoint or Spring's default error handling.
                log.warn("JWT Token processing failed for URI {}: {} - {}", request.getRequestURI(), e.getStatusCode(), e.getReason());
                SecurityContextHolder.clearContext(); // Ensure context is cleared on auth failure
                // Allow the filter chain to proceed so AuthenticationEntryPoint can handle it
            } catch (JwtException e) { // Catch any other specific JJWT exceptions if not wrapped by TokenService
                log.warn("Invalid JWT encountered for URI {}: {}", request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                log.error("Unexpected error during token authentication for URI {}: {}", request.getRequestURI(), e.getMessage(), e);
                SecurityContextHolder.clearContext();
            }
        } else {
            log.trace("SecurityContext already contains an authentication. Skipping JWT processing for URI: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}