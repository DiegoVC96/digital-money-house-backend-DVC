FROM maven:3.9-eclipse-temurin-21 AS build

ARG MODULE
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY users-service/pom.xml users-service/pom.xml
COPY account-service/pom.xml account-service/pom.xml
RUN mvn -q -pl ${MODULE} -am dependency:go-offline

COPY common common
COPY discovery-server discovery-server
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY users-service users-service
COPY account-service account-service
RUN mvn -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy

ARG MODULE
WORKDIR /app
RUN useradd --system --uid 10001 spring
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
