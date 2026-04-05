# 1. Build Stage: Use a Maven image that includes the JDK
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all files from your local directory to the container
COPY . .

# Build the jar using 'mvn' (which is pre-installed in this image)
RUN mvn clean package -DskipTests

# 2. Runtime Stage: Use a slim JRE image for the final container
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the generated jar file from the build stage
COPY --from=build /app/target/finance-backend-1.0.0.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
