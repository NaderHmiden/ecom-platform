# =============================================================================
# Stage 1 — Build
# Uses full Maven + JDK image. This layer is NEVER shipped to production.
# Dependency layer is cached separately so a code-only change doesn't
# re-download the entire Maven repository.
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy only the POM first — Docker caches this layer.
# A source-only change won't invalidate the dependency cache.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# =============================================================================
# Stage 2 — Runtime
# Minimal JRE on Alpine Linux (~80 MB vs ~600 MB for the builder image).
# Runs as a non-root user (principle of least privilege).
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

# Security: run as a dedicated non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the fat JAR produced by the builder stage
COPY --from=builder /app/target/ecom-platform-*.jar app.jar

# Ownership to non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Actuator health endpoint (used by Docker / K8s health checks)
EXPOSE 8080

# JVM tuning for containers:
# -XX:+UseContainerSupport  → respect container memory limits (not host RAM)
# -XX:MaxRAMPercentage=75   → use 75% of the container memory for heap
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
