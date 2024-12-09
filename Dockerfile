FROM gradle:8.5-jdk17 AS build

# Install Node.js and npm
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Install Tailwind CSS
WORKDIR /app
RUN npm install -D tailwindcss@latest

# Copy application files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src

# Build CSS with Tailwind
RUN ./node_modules/.bin/tailwindcss -i ./src/main/resources/static/styles/index.css -o ./src/main/resources/static/styles/output.css

# Build application
RUN gradle buildFatJar --no-daemon
RUN ls -la /app/build/libs/

# Final stage
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
RUN ls -la /app/app.jar && chmod +x /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]