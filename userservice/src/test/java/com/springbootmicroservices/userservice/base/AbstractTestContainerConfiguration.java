package com.springbootmicroservices.userservice.base;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractTestContainerConfiguration {

    // Using @Container will manage the lifecycle automatically with JUnit 5
    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:14-alpine");

    // This is not needed with @Container, but good to know:
    // @BeforeAll
    // static void beforeAll() {
    //     POSTGRES_CONTAINER.start();
    // }

    // This method dynamically sets the datasource properties for the test application context
    // before it starts, pointing it to the running test container.
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "create"); // Ensure schema is created
    }
}