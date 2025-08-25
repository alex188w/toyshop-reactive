# ===== Сборка приложения =====
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Устанавливаем Maven
RUN apt-get update && apt-get install -y maven

# Копируем pom.xml и исходники
COPY pom.xml .
COPY src ./src

# Собираем jar (без тестов для ускорения)
RUN mvn -B clean package -DskipTests

# ===== Запуск приложения =====
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Копируем собранный jar
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]