# Используем легковесный образ OpenJDK 17
FROM openjdk:17-jdk-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем весь проект внутрь контейнера
COPY . .

# Сборка проекта с пропуском тестов (если не нужно)
RUN ./mvnw clean package -DskipTests

# Новый слой с runtime (уменьшает размер образа)
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR из предыдущего контейнера
COPY --from=build /app/target/SNPBot-0.0.1-SNAPSHOT.jar app.jar

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
