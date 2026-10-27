# Production image for the Quizora API (Render).
# Build context: ./backend   Dockerfile: ./backend/Dockerfile

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Warm the dependency cache separately from the source tree.
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app

# Run as a non-root user, with a writable directory for the H2 fallback profile.
COPY --from=build --chown=app:app /workspace/target/*.jar app.jar
RUN mkdir -p /app/data && chown app:app /app/data
USER app

# Respect the container memory limit on Render's small instances.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
