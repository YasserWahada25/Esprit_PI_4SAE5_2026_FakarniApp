# Build autonome : depuis le dossier du module
#   docker build -t <image>:local .
# .dockerignore recommandé : target/ , .git
#
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B -e -q package -Dmaven.test.skip=true -Dfile.encoding=UTF-8

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -Dfile.encoding=UTF-8"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
