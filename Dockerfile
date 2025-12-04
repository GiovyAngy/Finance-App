# ---- BUILD Project ----
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copie pom.xml
COPY pom.xml .

# Dependency download
# RUN mvn -q dependency:go-offline

# Root
COPY src ./src

# New JAR
RUN mvn -q clean package -DskipTests


# ---- RUNTIME ----
FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# JAR
COPY --from=build /app/target/*.jar app.jar

# Port
EXPOSE 8081

# App start
ENTRYPOINT ["java", "-jar", "app.jar"]