# Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Копируем Maven wrapper и pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Загружаем зависимости (кэшируется отдельным слоем)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Финальный образ (мультистейдж сборка)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Установка утилит для healthcheck
RUN apk add --no-cache curl

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем JAR из builder stage
COPY --from=builder /app/target/*.jar app.jar

# Порт приложения
EXPOSE 8080

# JVM параметры
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
