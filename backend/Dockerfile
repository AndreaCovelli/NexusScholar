# Dockerfile for NexusScholar Backend (Spring Boot)

# --- STAGE 1: Build the application artifact ---
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app

# Copy the POM file and download dependencies (leverages Docker caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the JAR file
COPY src ./src
# Skip tests for the Docker build (CI already ran them)
RUN mvn clean package -DskipTests

# --- STAGE 2: Create the runtime image ---
# Use a lightweight JRE image
FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]