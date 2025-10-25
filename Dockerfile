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

# === Changed: mysqldump 설치 및 백업 디렉터리 생성/권한 설정 ===
# root 권한으로 패키지 설치 및 디렉터리 생성
USER root
RUN apt-get update && \
    # mysql-client와 mysql-server 설치
    apt-get install -y mysql-client mysql-server && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /opt/ravo/backup && \
    chown -R 1001:0 /opt/ravo/backup

COPY --from=builder /app/build/libs/*.jar app.jar

USER 1001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]