package com.springbootmicroservices.orderservice.model.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigurationParameter {

    // This PUBLIC_KEY value MUST BE THE PUBLIC KEY FROM YOUR USER_SERVICE
    // It's used here to validate tokens issued by UserService.
    AUTH_PUBLIC_KEY("""
            -----BEGIN PUBLIC KEY-----
                        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1HmZ3A379M6Rv9UnMt9R
                        Wq0a6bpcnoOWJxTi2exwnecW3r1X1PjeUvsDogy7RYjhlxU0G+1r38gPWfUW2FNd
                        tsa3H+FDhJ6dcNc4uKVYPsiVJukHi4NrvWA8E8dPdLW1lNcijr4PqXjvZTLoS1QX
                        f30wnNLBNDwdPXTESodi/n87VoSH2ChgLZUVfoS3m/NlUN8Z58gGxRcpUyjl+MmC
                        hD2cfyWr2xdxKd+UQMrd36LfyoWh0IlONlxo0H5x8JIwlziLbPEAh7dJ9QYM0b5G
                        msXBAzrILvW5+POSq4u1vNlzSwdLe+AZ6bnCwQVrqvMn/I7JT+4+lY8BsP/gMRQL
                        awIDAQAB
            -----END PUBLIC KEY-----
            """);
    // AUTH_ACCESS_TOKEN_EXPIRE_MINUTE("30"), // OrderService doesn't issue tokens, so not strictly needed
    // AUTH_REFRESH_TOKEN_EXPIRE_DAY("1");    // OrderService doesn't issue tokens, so not strictly needed

    private final String defaultValue;
}