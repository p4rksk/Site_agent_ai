FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar
EXPOSE 8080
CMD ["sh", "-c", "java -jar build/libs/*.jar"]