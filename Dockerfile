# ── Etapa 1: build ─────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Etapa 2: runtime ───────────────────────────────────────
FROM eclipse-temurin:21-jre

RUN groupadd -r m5group && useradd -r -g m5group m5user

WORKDIR /m5-storage

COPY --from=build /app/target/*.jar app.jar

ENV SERVER_PORT=8080

EXPOSE 8080

USER m5user

ENTRYPOINT ["java", "-jar", "app.jar"]