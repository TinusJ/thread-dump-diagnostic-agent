# Simple Dockerfile using pre-built JAR
FROM eclipse-temurin:21-jre-jammy

# Create a non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory 
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the pre-built JAR file
COPY target/thread-dump-diagnostic-agent-*.jar app.jar

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]