FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY main-application/pom.xml main-application/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY client-service/pom.xml client-service/pom.xml

RUN mvn dependency:go-offline -B

COPY main-application ./main-application

RUN mvn clean package -DskipTests -Dmaven.test.skip=true -pl main-application

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/main-application/target/*.jar app.jar

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]