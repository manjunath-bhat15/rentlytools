# Java 21 LTS
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy built jar
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
