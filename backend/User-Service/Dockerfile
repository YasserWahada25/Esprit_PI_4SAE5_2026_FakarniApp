FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B \
    -Dmaven.artifact.threads=1 \
    -Dhttp.connectionTimeout=300000 \
    -Dhttp.readTimeout=300000 \
    || mvn dependency:resolve -B
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]