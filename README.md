# booking-service
숙박 예약 웹서비스

## 구성
- booking-api: 예약 생성/취소, Outbox 작성, Redis 분산락
- inventory-svc: 재고 예약, Outbox 작성
- payment-svc: 모의 결제 승인/실패
- notification-svc: 알림 소비(로그)
- ranking-svc: 확정 이벤트 기반 Redis 랭킹 갱신(Stub)
- common: 이벤트 엔벨로프/타입

## 로컬 실행
1) 인프라 기동
```
docker compose -f infra/docker-compose.yml up -d
```

2) 서비스 실행(각 모듈)
```
./gradlew :modules:booking-api:bootRun
./gradlew :modules:inventory-svc:bootRun
./gradlew :modules:payment-svc:bootRun
./gradlew :modules:notification-svc:bootRun
./gradlew :modules:ranking-svc:bootRun
```

## 예약 API
```
POST http://localhost:8081/bookings
{
  "roomId": 1,
  "userId": 100,
  "startDate": "2025-01-01",
  "endDate": "2025-01-02"
}
```

## JMeter 동시성 테스트
- 스크립트: `infra/jmeter/booking-concurrency.jmx`
- 100 동시 요청, 동일 roomId/date, 응답 201 또는 409 허용

## 메모
- Outbox는 각 서비스별 테이블로 분리
- Kafka는 브로커 단독 구성(Confluent 이미지, Schema Registry 미사용)
- 이벤트 엔벨로프 버전 관리: `EventEnvelope.version`
# booking-spring
