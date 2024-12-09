# Build stage
FROM gradle:8.5-jdk17 AS build

# Cache Gradle dependencies
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN gradle dependencies --no-daemon

# Install Node.js and npm more efficiently
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get update && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/* && \
    npm install -g npm@latest

# Set up Tailwind CSS
WORKDIR /app
COPY package*.json ./
RUN npm install -D tailwindcss@latest

# Copy source files and build
COPY src src
RUN npm run tailwind && \
    gradle buildFatJar --no-daemon --parallel --build-cache

# Production stage
FROM ubuntu:22.04
WORKDIR /app

# Install Java and SQLite in a single layer
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    sqlite3 && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /data && \
    chmod 777 /data

# Copy application files
COPY --from=build /app/build/libs/*-all.jar app.jar
RUN chmod +x /app/app.jar

# Configure Java options for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Xmx512m -Dfile.encoding=UTF-8"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]