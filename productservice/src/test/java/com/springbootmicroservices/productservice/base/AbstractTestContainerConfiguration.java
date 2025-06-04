package com.springbootmicroservices.productservice.base;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractTestContainerConfiguration {

    static PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:14.6");

    @BeforeAll
    static void beforeAll() {
        POSTGRES_CONTAINER.withReuse(true);
        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    }
}
