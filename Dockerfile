# Use Maven + Java to build the app
FROM maven:3.9.1-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the source code
COPY pom.xml .
COPY src ./src

# Build the Spring Boot JAR
RUN mvn clean package -DskipTests

# Use slim Java runtime for running
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/spendsnap-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8081

# Run the app
ENTRYPOINT ["java","-jar","app.jar"]
