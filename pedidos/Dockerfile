
FROM maven:latest AS build
COPY src /app/src
COPY pom.xml /app
WORKDIR /app
RUN mvn clean package

FROM openjdk:22
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/pedidos-0.0.1-SNAPSHOT.jar ./app.jar
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]