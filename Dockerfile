# Multi-stage build for Spring Boot application

# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:resolve -B || \
    (echo "Retry 1/3: Maven dependency download failed, retrying..." && sleep 10 && mvn dependency:resolve -B) || \
    (echo "Retry 2/3: Maven dependency download failed, retrying..." && sleep 20 && mvn dependency:resolve -B) || \
    (echo "Retry 3/3: Maven dependency download failed, will try again during build..." && true)

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

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
