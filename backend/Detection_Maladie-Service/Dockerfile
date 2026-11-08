# ─── Stage 1 : Build ───────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilation
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2 : Runtime ─────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Sécurité : utilisateur non-root
RUN addgroup -S softcare && adduser -S softcare -G softcare
# MRI images are written to uploads/mri (relative to /app, owned by root): give the
# non-root user its own directory (also the mount point of the uploads volume).
RUN mkdir -p /app/uploads/mri && chown -R softcare:softcare /app/uploads
USER softcare

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8058

ENTRYPOINT ["java", "-jar", "app.jar"]