FROM eclipse-temurin:24-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
ENTRYPOINT ["java","-jar","target/drinkmonitor-0.0.1-SNAPSHOT.jar"]
