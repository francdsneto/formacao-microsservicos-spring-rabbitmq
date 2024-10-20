FROM maven:3.9.8-sapmachine-22 AS build
COPY src /app/src
COPY pom.xml /app
WORKDIR /app
RUN mvn clean package -DskipTests

FROM openjdk:22-slim
RUN apt-get update && apt-get install -y adduser curl default-mysql-client
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring
COPY --from=build /app/target/pedidos-0.0.1-SNAPSHOT.jar ./app.jar
COPY wait-for-mysql.sh ./wait-for-mysql.sh
ENTRYPOINT ["./wait-for-mysql.sh","java", "-jar", "/app.jar"]

