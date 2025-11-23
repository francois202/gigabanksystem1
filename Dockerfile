# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. Копируем только pom.xml сначала
COPY src/pom.xml .

# 2. Скачиваем зависимости (кешируется отдельно)
RUN mvn dependency:go-offline -B

# 3. Копируем исходники
COPY src ./src

# 4. Сборка с кешированием зависимостей
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаем не-root пользователя
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/target/*.jar app.jar

USER spring:spring
EXPOSE 8080

# Оптимизации для быстрого старта
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]