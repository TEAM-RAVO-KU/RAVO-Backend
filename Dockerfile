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
    # mysql-client 설치 (mysqldump 포함)
    apt-get install -y mysql-client && \
    # apt 캐시 삭제
    rm -rf /var/lib/apt/lists/* && \
    # 백업 디렉터리 생성
    mkdir -p /opt/ravo/backup && \
    # temurin 이미지의 기본 사용자(uid 1001, gid 0)에게 소유권 부여
    chown -R 1001:0 /opt/ravo/backup

# 산출물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# === Changed: 다시 non-root 사용자로 전환 ===
USER 1001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]