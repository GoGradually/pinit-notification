# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 비루트 실행
RUN useradd -r -u 10001 -g root appuser \
  && mkdir -p /app \
  && chown -R 10001:0 /app

# CI에서 ./gradlew clean generateProto test build 로 생성된 JAR을 가져옴
# plain.jar(라이브러리용) 제외하고 bootJar를 선택
COPY build/libs/*.jar /app/

RUN set -eux; \
  JAR="$(ls /app/*.jar | grep -v -- '-plain\.jar$' | head -n 1)"; \
  mv "$JAR" /app/app.jar; \
  rm -f /app/*-plain.jar || true; \
  chown 10001:0 /app/app.jar

USER 10001

# 실제 노출 포트는 k8s/deployment.yaml의 containerPort/Service가 기준
# (HTTP + gRPC 병행 가능성 고려: 필요한 포트만 남기면 됨)
EXPOSE 8080
EXPOSE 9090

ENTRYPOINT ["java","-jar","/app/app.jar"]
