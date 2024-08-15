# Build stage
FROM ubuntu:latest AS build
WORKDIR /app

# Install dependencies (with updated repository if needed)
RUN apt-get update && \
    apt-get install -y \
    openjdk-22-headless \
    maven

# Copy project files
COPY . .

# Build the application
RUN mvn clean package

# Runtime stage
FROM openjdk:22-jdk-slim

# Expose port for the application
EXPOSE 8080

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]