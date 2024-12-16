FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/Job-Platform-Website-0.0.1-SNAPSHOT.war app.war
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.war"]