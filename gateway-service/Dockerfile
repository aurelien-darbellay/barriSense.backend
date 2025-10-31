# =======================================
# 🧩 1. BUILD STAGE
# =======================================
FROM gradle:8.8-jdk21 AS builder

WORKDIR /workspace

# Copy entire Gradle context (root-level)
COPY gradle gradle
COPY gradlew gradlew.bat build.gradle settings.gradle* gradle.properties* ./

# Copy only the specific service (to limit rebuild scope)
ARG SERVICE_NAME
COPY ${SERVICE_NAME} ${SERVICE_NAME}

# Build that specific subproject's jar
RUN gradle :${SERVICE_NAME}:clean :${SERVICE_NAME}:bootJar --no-daemon

# =======================================
# ☕ 2. RUNTIME STAGE
# =======================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
ARG SERVICE_NAME

# Copy the built jar
COPY --from=builder /workspace/${SERVICE_NAME}/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java","-jar","app.jar"]
