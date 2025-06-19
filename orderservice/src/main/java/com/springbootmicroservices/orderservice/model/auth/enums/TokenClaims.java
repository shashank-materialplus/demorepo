package com.springbootmicroservices.orderservice.model.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenClaims {
    JWT_ID("jti"),
    USER_ID("userId"),          // Crucial for OrderService to identify the user
    USER_TYPE("userType"),      // For Admin checks (e.g., hasAuthority('ADMIN'))
    USER_STATUS("userStatus"),
    USER_FIRST_NAME("userFirstName"),
    USER_LAST_NAME("userLastName"),
    USER_EMAIL("userEmail"),        // Used in BaseEntity for createdBy/updatedBy
    USER_PHONE_NUMBER("userPhoneNumber"),
    // STORE_TITLE("storeTitle"), // Likely not relevant for OrderService claims
    ISSUED_AT("iat"),
    EXPIRES_AT("exp"),
    ALGORITHM("alg"),
    TYP("typ");

    private final String value;
}