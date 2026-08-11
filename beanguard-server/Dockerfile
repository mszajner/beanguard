# Build from the repo root: docker build -f beanguard-server/Dockerfile .
# beanguard-server depends on beanguard-api (compile) and beanguard-client
# (test scope), both reactor modules - the build context has to be the
# whole repo, not just this directory, so Maven can see the parent pom and
# sibling modules. .dockerignore keeps it to just what's needed.

FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy POMs first so dependency resolution is its own cached layer,
# independent of source changes.
COPY pom.xml .
COPY beanguard-api/pom.xml beanguard-api/pom.xml
COPY beanguard-client/pom.xml beanguard-client/pom.xml
COPY beanguard-server/pom.xml beanguard-server/pom.xml
RUN mvn -B -pl beanguard-server -am dependency:go-offline

COPY beanguard-api/src beanguard-api/src
COPY beanguard-client/src beanguard-client/src
COPY beanguard-server/src beanguard-server/src
RUN mvn -B -pl beanguard-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
ENV TZ=Europe/Warsaw
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt-get update && apt-get install -y --no-install-recommends bash wget \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /
COPY --from=builder /build/beanguard-server/target/*.jar app.jar
COPY beanguard-server/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
