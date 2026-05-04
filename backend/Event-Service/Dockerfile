# ─── Stage 1 : Build ───────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances Maven avec timeout augmenté
COPY pom.xml .
RUN mvn dependency:go-offline -B \
    -Dmaven.artifact.threads=1 \
    -Dhttp.connectionTimeout=300000 \
    -Dhttp.readTimeout=300000 \
    || mvn dependency:resolve -B

# Compilation
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2 : Runtime ─────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Sécurité : utilisateur non-root
RUN addgroup -S softcare && adduser -S softcare -G softcare
USER softcare

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
