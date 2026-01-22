# Use Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk
LABEL authors="14158"

# Set working directory inside the container
WORKDIR /app

# Copy the built jar into the container
COPY target/ilp_cw3-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 for REST service
EXPOSE 8080

# Run the Java application
ENTRYPOINT ["java", "-jar", "app.jar"]
