# Build from this directory: docker build -f beanguard-demo/Dockerfile beanguard-demo
# beanguard-demo is a standalone Maven project (its own pom.xml, not a
# reactor module) that depends on a published beanguard-client release from
# Maven Central - it doesn't need the rest of the repo as build context.

FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy the POM first so dependency resolution is its own cached layer,
# independent of source changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre-jammy
ENV TZ=Europe/Warsaw
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt-get update && apt-get install -y --no-install-recommends bash wget \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /
COPY --from=builder /build/target/*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
