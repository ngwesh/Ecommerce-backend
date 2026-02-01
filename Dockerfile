# Stage 1: Build using Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime using JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Adjust 'target/*.jar' if your build produces a specific filename
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
