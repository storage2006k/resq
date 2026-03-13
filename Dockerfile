# ═══════════════════════════════════════════════════
# RESQ — Multi-stage Production Dockerfile
# Stage 1: Build JAR with Maven
# Stage 2: Run with minimal JRE Alpine image
# ═══════════════════════════════════════════════════

# ── Stage 1: Build ──
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# ── Stage 2: Run ──
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S resq && adduser -S resq -G resq

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

# Create log directory
RUN mkdir -p /var/log/resq && chown resq:resq /var/log/resq

# Switch to non-root
USER resq

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
