# Build stage
FROM maven:3.9.1-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/target/spendsnap-0.0.1-SNAPSHOT.jar app.jar

# Render dynamically assigns PORT
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
