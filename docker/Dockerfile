# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy POM first so dependency downloads can be cached
COPY pom.xml .

RUN mvn -B dependency:go-offline

# Copy source code after dependencies
COPY src ./src

RUN mvn -B -DskipTests package

# Runtime stage
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

WORKDIR /work

COPY --from=build --chown=185:0 /workspace/target/quarkus-app/lib/ ./lib/
COPY --from=build --chown=185:0 /workspace/target/quarkus-app/*.jar ./
COPY --from=build --chown=185:0 /workspace/target/quarkus-app/app/ ./app/
COPY --from=build --chown=185:0 /workspace/target/quarkus-app/quarkus/ ./quarkus/

USER 185

ENV QUARKUS_HTTP_HOST=0.0.0.0

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]