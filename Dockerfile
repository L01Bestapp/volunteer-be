# Multi-stage build for Spring Boot application

# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
# This layer is cached unless pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B || mvn dependency:resolve -B || true

# Copy source code and build
# Only this layer rebuilds when source code changes
COPY src ./src
RUN mvn package -DskipTests -B -o 2>/dev/null || mvn package -DskipTests -B

# Stage 2: Runtime stage - Use minimal JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM Memory Optimization for 512MB RAM environment
# Total RAM: 512MB
# OS + processes: ~100MB
# JVM available: ~400MB max
# Heap: 300MB max (leave room for metaspace, code cache, threads)
ENV JAVA_TOOL_OPTIONS="\
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "app.jar"]
