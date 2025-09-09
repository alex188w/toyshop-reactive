# FROM eclipse-temurin:21-jdk AS build
# WORKDIR /app
# COPY . .
# RUN ./mvnw clean package -DskipTests

# FROM eclipse-temurin:21-jre
# WORKDIR /app
# COPY --from=build /app/target/paymentService-0.0.1-SNAPSHOT.jar app.jar
# EXPOSE 8081
# ENTRYPOINT ["java","-jar","app.jar"]

# Используем готовый JRE для запуска
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем готовый JAR (собранный на хосте)
COPY target/paymentService-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]