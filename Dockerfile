FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE $PORT
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]