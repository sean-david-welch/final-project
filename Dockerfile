# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy as deps

WORKDIR /build

# Copy Gradle files instead of Maven
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

################################################################################

FROM deps as package

WORKDIR /build

# Copy source code
COPY ./src src/

# Build the application
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon && \
    mv build/libs/*.jar build/libs/app.jar

################################################################################

FROM eclipse-temurin:21-jre-jammy AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the jar from the package stage
COPY --from=package build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]