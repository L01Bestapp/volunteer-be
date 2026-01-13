# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Dependency Caching Layer (Tối ưu nhất)
COPY pom.xml .
# Lệnh này tải toàn bộ dependencies và plugins cần thiết
# Không cần dùng "|| true" hay "-o", hãy để nó fail nếu mạng lỗi để biết đường sửa
RUN mvn dependency:go-offline -B

# 2. Build Layer
COPY src ./src
# Build package và đổi tên file output thành app.jar ngay tại đây để dễ copy
RUN mvn clean package -DskipTests -B && \
    cp target/*.jar app.jar

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Setup non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy file jar đã được đổi tên (Tránh dùng *.jar dễ copy nhầm file)
COPY --from=build /app/app.jar app.jar

EXPOSE 8080

# Health check (Giữ nguyên vì đã tốt)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM Optimization
# - Đã xóa dòng /dev/./urandom (Java 17+ không cần nữa)
# - Thêm ExitOnOutOfMemoryError: Để container tự restart nếu hết RAM (thay vì bị treo)
ENV JAVA_TOOL_OPTIONS="\
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+UseStringDeduplication"

# Entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]