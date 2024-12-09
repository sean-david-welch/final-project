FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/build/libs/budget-ai.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]