# syntax=docker/dockerfile:1
# Backend TechGear: build jar bằng Maven rồi chạy trên JRE 21. Test chạy riêng (mvn verify), không chạy trong lúc build ảnh.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S techgear && adduser -S techgear -G techgear \
    && mkdir uploads && chown techgear:techgear uploads
COPY --from=build /src/target/techgear-backend-*.jar app.jar
USER techgear
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
