package com.springbootmicroservices.orderservice.model.common.entity;

import com.springbootmicroservices.orderservice.model.auth.enums.TokenClaims; // Make sure TokenClaims enum is present
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt; // For casting the principal

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@SuperBuilder // Allows subclasses to use @Builder and include these fields
@MappedSuperclass // Indicates this is a base class for entities, not an entity itself
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY", updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_AT", nullable = false) // Set on create and update
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @PrePersist
    protected void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now; // Set updatedAt on creation as well

        String userIdentifier = getAuthenticatedUserIdentifier().orElse("SYSTEM");
        this.createdBy = userIdentifier;
        this.updatedBy = userIdentifier; // Set updatedBy on creation
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = getAuthenticatedUserIdentifier().orElse("SYSTEM_UPDATE"); // Or just SYSTEM
    }

    /**
     * Retrieves the identifier (e.g., email or user ID) of the currently authenticated user
     * from the JWT token in the SecurityContext.
     *
     * @return Optional containing the user identifier (e.g., email) if authenticated, otherwise empty.
     */
    private Optional<String> getAuthenticatedUserIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            // Prioritize USER_ID if available, otherwise USER_EMAIL
            String userId = jwt.getClaimAsString(TokenClaims.USER_ID.getValue());
            if (userId != null && !userId.isBlank()) {
                return Optional.of(userId);
            }
            String userEmail = jwt.getClaimAsString(TokenClaims.USER_EMAIL.getValue());
            if (userEmail != null && !userEmail.isBlank()) {
                return Optional.of(userEmail);
            }
            // Fallback to subject if other identifiers are missing
            return Optional.ofNullable(jwt.getSubject());

        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // Fallback if using UserDetails (less common in pure JWT microservices)
            return Optional.of(((org.springframework.security.core.userdetails.User) principal).getUsername());
        } else if (principal instanceof String) {
            // If principal is just the username string (e.g., from some very custom auth)
            return Optional.of((String) principal);
        }

        // If principal is of an unknown type or user identifier cannot be extracted
        return Optional.empty();
    }
}