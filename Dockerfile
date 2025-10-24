### 1) Build stage (JDK 17로 변경)
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Gradle wrapper 먼저 복사 -> 캐시 효율
COPY gradlew .
COPY gradle/ ./gradle/
COPY build.gradle .
COPY settings.gradle .

# 소스 복사
COPY src/ ./src/

# 실행 권한
RUN chmod +x gradlew

# (옵션) 빌드 캐시 활용
# RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon clean build -x test
RUN ./gradlew clean build -x test

### 2) Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /opt/app

# 산출물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]