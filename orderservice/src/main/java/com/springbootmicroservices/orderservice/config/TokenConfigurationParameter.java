package com.springbootmicroservices.orderservice.config;

import com.springbootmicroservices.orderservice.model.user.enums.ConfigurationParameter;
import com.springbootmicroservices.orderservice.utils.KeyConverter; // You will create this util
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.security.PublicKey;
// PrivateKey is not needed if OrderService only validates tokens
// import java.security.PrivateKey;


@Getter
@Configuration
public class TokenConfigurationParameter {

    // OrderService primarily needs the public key to validate tokens.
    // It does not issue tokens, so it doesn't need the private key or token expiry for issuing.
    private final PublicKey publicKey;
    // private final int accessTokenExpireMinute; // Not strictly needed by OrderService
    // private final PrivateKey privateKey; // Not needed by OrderService

    public TokenConfigurationParameter() {
        this.publicKey = KeyConverter.convertPublicKey(
                ConfigurationParameter.AUTH_PUBLIC_KEY.getDefaultValue()
        );

        // These would only be needed if OrderService was also issuing tokens or needed to check expiry itself
        // based on its own config, but token validation usually checks the 'exp' claim directly.
        // this.accessTokenExpireMinute = Integer.parseInt(
        //        ConfigurationParameter.AUTH_ACCESS_TOKEN_EXPIRE_MINUTE.getDefaultValue()
        // );
    }
}