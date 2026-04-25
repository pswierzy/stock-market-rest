FROM ghcr.io/graalvm/native-image-community:25 AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

ENV _JAVA_OPTIONS="-Xmx4g"

RUN chmod +x mvnw
RUN ./mvnw -Pnative native:compile -DskipTests

FROM alpine:latest
RUN apk add --no-cache curl libc6-compat
WORKDIR /app

COPY --from=build /app/target/stock-market app

EXPOSE 8080

ENTRYPOINT ["/app/app"]