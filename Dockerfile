# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

# Build (skip tests — they run in CI before deploy)
COPY src ./src
RUN ./mvnw package -DskipTests -B -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Bundle TLS certificates (used by Netty)
COPY src/main/resources/server-test.crt /app/certs/server.crt
COPY src/main/resources/server-test.key /app/certs/server.key
COPY src/main/resources/ca-test.crt     /app/certs/ca.crt
COPY src/main/resources/ca-test.key     /app/certs/ca.key

EXPOSE 8080
EXPOSE 9000

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
