# ─── Stage 1: Build ───────────────────────────────────────────
# Use full JDK + Maven to compile and package
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# set working directory inside the build container
WORKDIR /app

# copy pom.xml first — Docker caches this layer
# if pom.xml hasn't changed, Maven dependencies aren't re-downloaded
# this makes subsequent builds much faster
COPY pom.xml .
RUN mvn dependency:go-offline -B

# copy source code
COPY src ./src

# build the JAR, skip tests (tests run in CI pipeline separately)
RUN mvn clean package -DskipTests

# ─── Stage 2: Run ─────────────────────────────────────────────
# Use slim JRE only — no Maven, no JDK, much smaller image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# create a non-root user — security best practice
# never run production apps as root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# copy only the built JAR from stage 1
# the wildcard handles version numbers in JAR name
COPY --from=builder /app/target/*.jar app.jar

# document which port this service uses
EXPOSE 8083

# health check — Docker monitors this and marks container unhealthy if it fails
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8083/actuator/health || exit 1

# start the application
# -Djava.security.egd improves startup speed on Linux containers
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]