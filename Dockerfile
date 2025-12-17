FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY main-application/pom.xml main-application/
COPY account-service/pom.xml account-service/
COPY client-service/pom.xml client-service/

RUN mvn dependency:resolve -B

COPY . .

RUN mvn clean package -DskipTests -pl main-application -am

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder --chown=spring:spring /app/main-application/target/*.jar app.jar

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]