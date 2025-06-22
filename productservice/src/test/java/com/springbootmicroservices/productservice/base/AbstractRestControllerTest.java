package com.springbootmicroservices.productservice.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootmicroservices.productservice.client.UserServiceClient;
import com.springbootmicroservices.productservice.model.auth.enums.TokenClaims;
import com.springbootmicroservices.productservice.model.auth.UserStatus;
import com.springbootmicroservices.productservice.model.auth.UserType;
import org.junit.jupiter.api.BeforeEach;
import com.springbootmicroservices.productservice.model.auth.UserStatus;
import com.springbootmicroservices.productservice.model.auth.UserType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractRestControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    // We will generate simple mock tokens, as the content doesn't matter, only the mock response from UserServiceClient
    protected static final String MOCK_ADMIN_TOKEN = "mockAdminToken";
    protected static final String MOCK_USER_TOKEN = "mockUserToken";

    @BeforeEach
    public void setupAuthenticationMocks() {
        // Mock the ADMIN authentication response
        Jwt adminJwt = createMockJwt("admin-id-123", UserType.ADMIN);
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                adminJwt, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        Mockito.when(userServiceClient.getAuthentication(MOCK_ADMIN_TOKEN)).thenReturn(adminAuth);
        Mockito.doNothing().when(userServiceClient).validateToken(MOCK_ADMIN_TOKEN);

        // Mock the USER authentication response
        Jwt userJwt = createMockJwt("user-id-456", UserType.USER);
        UsernamePasswordAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(
                userJwt, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Mockito.when(userServiceClient.getAuthentication(MOCK_USER_TOKEN)).thenReturn(userAuth);
        Mockito.doNothing().when(userServiceClient).validateToken(MOCK_USER_TOKEN);
    }

    private Jwt createMockJwt(String userId, UserType userType) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim(TokenClaims.USER_ID.getValue(), userId)
                .claim(TokenClaims.USER_TYPE.getValue(), userType.name())
                .claim(TokenClaims.USER_STATUS.getValue(), UserStatus.ACTIVE.name())
                .claim(TokenClaims.USER_EMAIL.getValue(), "test@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}