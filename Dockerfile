# Multi-stage build for production-ready image
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (for better layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Create non-root user for security
RUN groupadd -r ragchat && useradd -r -g ragchat ragchat

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to ragchat user
RUN chown ragchat:ragchat app.jar

# Switch to non-root user
USER ragchat

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health/ping || exit 1

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]