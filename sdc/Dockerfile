FROM eclipse-temurin:21-jdk-alpine AS base
WORKDIR app
RUN apk add --no-cache maven

FROM base AS build
COPY . .
RUN mvn install

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]