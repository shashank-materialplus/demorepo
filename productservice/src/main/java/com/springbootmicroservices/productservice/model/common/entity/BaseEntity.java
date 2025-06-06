package com.springbootmicroservices.productservice.model.common.entity;

import com.springbootmicroservices.productservice.model.auth.enums.TokenClaims;
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
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Base entity class named {@link BaseEntity} with common fields for audit tracking and lifecycle management.
 * Provides automatic population of audit fields using JPA lifecycle annotations.
 */
@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /**
     * Sets the createdBy and createdAt fields before persisting the entity.
     * If no authenticated user is found, sets createdBy to "anonymousUser".
     */
    @PrePersist
    public void prePersist() {
        // Safer version
        this.createdBy = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> !"anonymousUser".equals(principal.toString())) // Check against toString() for anonymousUser
                .filter(Jwt.class::isInstance) // Add this filter
                .map(Jwt.class::cast)
                .map(jwt -> jwt.getClaimAsString(TokenClaims.USER_EMAIL.getValue())) // Use getClaimAsString for safety
                .orElse("anonymousUser");
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Sets the updatedBy and updatedAt fields before updating the entity.
     * If no authenticated user is found, sets updatedBy to "anonymousUser".
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedBy = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(user -> !"anonymousUser".equals(user))
                .map(Jwt.class::cast)
                .map(jwt -> jwt.getClaim(TokenClaims.USER_EMAIL.getValue()).toString())
                .orElse("anonymousUser");
        this.updatedAt = LocalDateTime.now();
    }

}
