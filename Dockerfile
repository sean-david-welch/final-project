FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
