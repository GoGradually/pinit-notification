# Pinit Notification 🛎️

> **핀잇 알림 도메인**을 전담하는 마이크로서비스. 일정(Task) 서비스와 분리되어 스케줄 이벤트를 받아 사용자에게 푸시 알림을 전달합니다.

---

## 🎯 Purpose

### 1) 알림 바운디드 컨텍스트 분리

- 일정/작업 도메인(`task-service`)과 분리된 **독립 서비스**로 동작합니다.
- 스케줄 변경에 따른 알림 생성/취소/갱신을 이곳에서만 책임지며, **ID/토큰 수준**으로만 다른 서비스와 연결합니다.

### 2) 이벤트 드리븐 & 멱등 처리

- RabbitMQ `task.schedule.direct` 익스체인지에서 이벤트를 수신해 알림을 예약합니다.
- `idempotentKey` + 복합 유니크 키(소유자, 스케줄)로 **중복 예약을 방지**하고 재처리를 안전하게 합니다.

### 3) 채널 확장성을 고려한 푸시 기반

- 기본 채널은 **FCM 푸시**이며, 웹 푸시용 **VAPID 공개키**를 제공합니다.
- 알림 데이터(`UpcomingScheduleNotification`)는 채널에 의존하지 않는 구조로 설계돼 이메일/SMS 등 확장이 용이합니다.

---

## 🧱 Architecture Overview

```
Task Service (schedule) --gRPC--> Notification (schedule basics 조회)
Task Service --RabbitMQ(task.schedule.direct)--> Notification --MySQL--> 알림 저장
                                               \--FCM--> 사용자 디바이스
```

- gRPC: 스케줄 제목/시작시각을 원본 서비스에서 조회하여 알림 페이로드를 신뢰성 있게 구성.
- RabbitMQ: 스케줄 상태 변경(업데이트/삭제/시작/취소/완료) 이벤트를 비동기로 처리.
- Scheduler: 10분 주기(`0 */10 * * * *`)로 만료된 알림을 찾아 푸시 전송 후 정리.

---

## 🧩 Domain Model

- `UpcomingScheduleNotification`
    - 필드: ownerId, scheduleId, scheduleTitle, scheduleStartTime(ISO-8601 문자열), idempotentKey
    - 역할: 특정 시점(시작 시간)에 도달하면 발송해야 하는 알림 예약. `isDue` 로 만기 판단.

- `PushSubscription`
    - 필드: memberId, token (FCM)
    - 역할: 회원별 푸시 채널 정보. 만료/UNREGISTERED 발생 시 토큰을 정리합니다.

- `Notification` 인터페이스
    - `getData()` 로 채널에 독립적인 데이터 맵을 제공해 향후 채널 확장 시 재사용.

---

## 🧰 Tech Stack

- Java 21, Gradle, Spring Boot 3.5 (Web, Security, Data JPA, Scheduler, Actuator)
- 메시징: Spring AMQP + RabbitMQ
- 통신: gRPC 클라이언트(`ScheduleGrpcServiceBlockingStub`)로 스케줄 기본 정보 조회
- Push: Firebase Admin SDK, 웹 푸시 VAPID 키
- DB: MySQL (테스트용 H2), JPA
- 문서화: springdoc-openapi / Swagger UI

---

## 🔑 Main Features

1) **스케줄 이벤트 처리**
    - 라우팅키: `schedule.time.upcoming.updated`, `schedule.deleted`, `schedule.state.started|canceled|completed`
    - 이벤트마다 알림 예약을 생성/갱신/삭제해 실제 스케줄 상태와 알림을 동기화.

2) **알림 예약/발송**
    - 예약: `ScheduleNotificationService` 가 이벤트를 받아 `UpcomingScheduleNotification` 생성.
    - 발송: `NotificationDispatchScheduler` 가 만기 알림을 찾고, 회원의 모든 토큰에 전송 후 일괄 삭제.

3) **푸시 구독 관리**
    - API: `/push/subscribe`, `/push/unsubscribe` 로 FCM 토큰 등록/삭제.
    - `/push/vapid` 로 웹 푸시 공개키 제공.
    - UNREGISTERED 오류 시 토큰 자동 정리.

4) **보안/인증 연동**
    - JWT 필터(`JwtAuthenticationFilter`, `MemberIdArgumentResolver`)로 인증된 사용자만 구독/발송 경로 접근.
    - CORS 허용 도메인과 키 경로를 프로파일별(`application-{dev,prod}.yml`)로 분리 관리.

---

## ⚙️ Configuration & Run

필수 환경 변수 (예시, dev 기준):

- DB 접속: `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
- RabbitMQ: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
- gRPC Task: `task.grpc.host`, `task.grpc.port` (yml로 주입)
- Firebase 키 경로: `${HOME}/pinit/keys/pinit-firebase-key.json`
- JWT 공개키 경로: `${HOME}/pinit/keys/jwt-public-key.pem`
- VAPID: `VAPID_PRIVATE_KEY` (공개키는 `application.yml`에 정의)

로컬 실행:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Swagger UI:

```
http://localhost:8082/swagger-ui.html
```

---

## 🧭 Design Principles

- **느슨한 결합**: 알림 도메인은 이벤트/ID/토큰 단위로만 다른 서비스와 연결, 내부 모델을 공유하지 않음.
- **멱등 & 신뢰성**: `idempotentKey` 기반 중복 방지, Durable 큐와 일괄 삭제로 일관성 유지.
- **관찰성/운영**: Actuator 헬스체크, 스케줄러 로그, UNREGISTERED 토큰 자동 정리로 운영 부담 감소.
- **확장 가능 채널**: Notification 인터페이스와 데이터맵 구조로 이메일/SMS 같은 채널 추가 용이.
