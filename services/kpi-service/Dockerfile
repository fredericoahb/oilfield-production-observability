FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy aggregator + modules (needed for parent POM resolution in Docker build)
COPY pom.xml /workspace/pom.xml
COPY services /workspace/services

# Build only this module (and required reactor deps, if any)
RUN mvn -q -DskipTests -pl services/kpi-service -am package \
 && JAR=$(ls /workspace/services/kpi-service/target/*.jar | grep -v 'original-' | head -n 1) \
 && echo "Using jar: $JAR" \
 && cp "$JAR" /workspace/app.jar
FROM eclipse-temurin:21-jre
WORKDIR /app

# OpenTelemetry Java Agent (pinned): v2.25.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.25.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

COPY --from=build /workspace/app.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS:-} -jar /app/app.jar"]
