# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/redis-url-shortener-1.0.0-SNAPSHOT.war app.war

ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -jar app.war --server.port=${PORT}"]
