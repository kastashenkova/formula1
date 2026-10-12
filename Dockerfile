FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY backend/custom-notifier-starter/pom.xml backend/custom-notifier-starter/pom.xml
COPY backend/identity/pom.xml backend/identity/pom.xml
COPY backend/processing/pom.xml backend/processing/pom.xml
COPY backend/streaming/pom.xml backend/streaming/pom.xml
COPY backend/application/pom.xml backend/application/pom.xml
COPY frontend/pom.xml frontend/pom.xml

RUN ./mvnw dependency:go-offline -B

COPY . .
RUN ./mvnw clean package -DskipTests -o

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/application/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]