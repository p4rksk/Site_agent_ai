FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar
EXPOSE 8080
CMD ["java", "-jar", "build/libs/site-agent-backend-0.0.1-SNAPSHOT.jar"]