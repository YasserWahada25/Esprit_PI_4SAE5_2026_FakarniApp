# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies with increased timeout
COPY pom.xml .
RUN mvn dependency:go-offline -B \
    -Dmaven.artifact.threads=1 \
    -Dhttp.connectionTimeout=300000 \
    -Dhttp.readTimeout=300000 \
    || mvn dependency:resolve -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
