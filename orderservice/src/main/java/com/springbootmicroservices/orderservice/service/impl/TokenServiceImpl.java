package com.springbootmicroservices.orderservice.service.impl;

import com.springbootmicroservices.orderservice.config.TokenConfigurationParameter;
import com.springbootmicroservices.orderservice.model.auth.enums.TokenClaims; // Your enum
import com.springbootmicroservices.orderservice.service.TokenService;
import io.jsonwebtoken.*; // Includes Claims, Jws, JwtException, ExpiredJwtException, etc.
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final TokenConfigurationParameter tokenConfigurationParameter;

    private Jws<Claims> parseAndValidateJws(String jwt) {
        if (jwt == null || jwt.isBlank()) {
            log.warn("JWT string is null or empty.");
            throw new BadCredentialsException("JWT token cannot be null or empty.");
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(tokenConfigurationParameter.getPublicKey())
                    .build()
                    .parseClaimsJws(jwt);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired. Token Snippet: [{}...], Details: {}", jwt.substring(0, Math.min(jwt.length(), 20)), e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported. Details: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unsupported JWT token.", e);
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed. Details: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token format.", e);
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed. Details: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT signature.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT argument was invalid for parsing. Details: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JWT argument for parsing.", e);
        } catch (Exception e) {
            log.error("Unexpected error validating token. Details: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating token.", e);
        }
    }

    @Override
    public void validateToken(final String jwt) {
        parseAndValidateJws(jwt);
        log.debug("Token validated successfully.");
    }

    @Override
    public UsernamePasswordAuthenticationToken getAuthentication(final String jwt) {
        Jws<Claims> claimsJws = parseAndValidateJws(jwt);
        Claims payload = claimsJws.getBody(); // Use getBody()

        String userId = payload.get(TokenClaims.USER_ID.getValue(), String.class);
        if (userId == null || userId.isBlank()) {
            log.warn("USER_ID claim ('{}') missing or blank in token.", TokenClaims.USER_ID.getValue());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_ID claim is missing or blank from token.");
        }

        Map<String, Object> headers = new java.util.HashMap<>();
        if (claimsJws.getHeader().getAlgorithm() != null) {
            headers.put("alg", claimsJws.getHeader().getAlgorithm());
        }
        if (claimsJws.getHeader().getType() != null) {
            headers.put("typ", claimsJws.getHeader().getType());
        } else {
            headers.put("typ", "JWT");
        }

        Instant issuedAtInstant = payload.getIssuedAt() != null ? payload.getIssuedAt().toInstant() : null;
        if (issuedAtInstant == null) {
            log.warn("IssuedAt (iat) claim is missing in the JWT. This is unusual and may affect principal creation.");
            // Consider throwing an error if 'iat' is strictly mandatory:
            // throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "IssuedAt (iat) claim is missing from token.");
            // Or use a default, though it's not ideal for a real timestamp:
            // issuedAtInstant = Instant.now();
        }

        Instant expiresAtInstant = payload.getExpiration() != null ? payload.getExpiration().toInstant() : null;

        Jwt springJwtPrincipal = new Jwt(
                jwt,
                issuedAtInstant,
                expiresAtInstant,
                headers,
                payload
        );

        Collection<GrantedAuthority> authorities = Collections.emptyList();
        Object userTypeClaim = payload.get(TokenClaims.USER_TYPE.getValue());

        if (userTypeClaim instanceof String) {
            String roleString = ((String) userTypeClaim).toUpperCase();
            authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleString));
            log.debug("Authorities set from USER_TYPE String claim: {}", authorities);
        } else if (userTypeClaim instanceof List) {
            try {
                authorities = ((List<?>) userTypeClaim).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + String.valueOf(role).toUpperCase()))
                        .collect(Collectors.toList());
                log.debug("Authorities set from USER_TYPE List claim: {}", authorities);
            } catch (Exception e) {
                log.warn("Could not parse roles from USER_TYPE claim list: {}. Error: {}", userTypeClaim, e.getMessage());
            }
        } else if (userTypeClaim != null) {
            log.warn("USER_TYPE claim ('{}') found but is not a String or List. Value: {}", TokenClaims.USER_TYPE.getValue(), userTypeClaim);
        } else {
            log.warn("USER_TYPE claim ('{}') not found in token. No specific authorities will be set beyond authenticated.", TokenClaims.USER_TYPE.getValue());
        }

        return new UsernamePasswordAuthenticationToken(springJwtPrincipal, null, authorities);
    }

    @Override
    public <T> T getClaim(final String jwt, final String claimName, Class<T> expectedType) {
        Claims claims = getAllClaims(jwt);
        T claimValue = claims.get(claimName, expectedType);
        if (claimValue == null) {
            log.warn("Claim '{}' not found in token or type mismatch (expected {}).", claimName, expectedType.getSimpleName());
        }
        return claimValue;
    }

    @Override
    public Claims getAllClaims(final String jwt) {
        return parseAndValidateJws(jwt).getBody(); // Use getBody()
    }

    @Override
    public String getUserIdFromToken(final String jwt) {
        String userId = getClaim(jwt, TokenClaims.USER_ID.getValue(), String.class);
        if (userId == null || userId.isBlank()) {
            log.error("Critical: USER_ID claim ('{}') is missing or blank from token after validation.", TokenClaims.USER_ID.getValue());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_ID claim is missing or blank from token.");
        }
        return userId;
    }

    @Override
    public String getJtiFromToken(final String jwt) {
        Claims payload = getAllClaims(jwt);
        String jti = payload.getId();     // payload.getId() correctly gets the "jti" claim value
        if (jti == null) {
            // For logging the claim name, you can use your TokenClaims enum or Claims.ID
            log.warn("JTI (JWT ID) claim ('{}') missing in token.", TokenClaims.JWT_ID.getValue());
            // Alternatively, using the standard constant if preferred for the log message:
            // log.warn("JTI (JWT ID) claim ('{}') missing in token.", Claims.ID);
        }
        return jti;
    }
}