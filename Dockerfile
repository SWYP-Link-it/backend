# 빌드 스테이지
FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# Gradle 파일 복사 (캐싱 최적화)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]