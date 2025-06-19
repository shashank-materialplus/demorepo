package com.springbootmicroservices.orderservice.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface TokenService {

    /**
     * Verifies the signature, expiration, and basic structure of the JWT.
     * Throws an exception if the token is invalid.
     *
     * @param jwt the JWT string to validate.
     */
    void validateToken(final String jwt);

    /**
     * Parses the JWT, validates it, and extracts claims to build
     * an {@link UsernamePasswordAuthenticationToken} for Spring Security context.
     *
     * @param jwt the JWT string.
     * @return an authenticated {@link UsernamePasswordAuthenticationToken}.
     */
    UsernamePasswordAuthenticationToken getAuthentication(final String jwt);

    /**
     * Extracts a specific claim from the JWT payload after validation.
     *
     * @param jwt the JWT string.
     * @param claimName the name of the claim to extract (e.g., from TokenClaims enum).
     * @param expectedType the class type of the expected claim value.
     * @return the claim value, or null if not found or type mismatch.
     * @param <T> the type of the claim value.
     */
    <T> T getClaim(final String jwt, final String claimName, Class<T> expectedType);

    /**
     * Extracts the entire claims set from the JWT payload after validation.
     *
     * @param jwt the JWT string.
     * @return the {@link Claims} object.
     */
    Claims getAllClaims(final String jwt);

    /**
     * Extracts the User ID (typically 'userId' or 'sub') from the JWT.
     *
     * @param jwt the JWT string.
     * @return the User ID.
     */
    String getUserIdFromToken(final String jwt);

    /**
     * Extracts the JTI (JWT ID) from the JWT.
     * (OrderService might not need JTI directly unless it's involved in some advanced token tracking)
     * @param jwt the JWT string
     * @return the JTI
     */
    String getJtiFromToken(final String jwt);
}