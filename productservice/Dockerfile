# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Copy Maven files for dependency resolution
COPY pom.xml ./
COPY .mvn .mvn

# Copy application source code
COPY src src

# Build the project and create the executable JAR
RUN mvn clean install -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:21

# Set working directory
WORKDIR productservice

# Copy the JAR file from the build stage
COPY --from=build target/*.jar productservice.jar

# Expose port 1111
EXPOSE 1111

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "-jar", "productservice.jar"]