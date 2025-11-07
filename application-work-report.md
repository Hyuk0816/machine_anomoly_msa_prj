# 🤖 실시간 스마트 팩토리 예지보전 MSA 프로젝트 구현 진행 보고서

## 프로젝트 개요
- **프로젝트명**: Real-time Smart Factory Predictive Maintenance MSA System
- **구현 계획 수립**: 2025.11.07 16:30
- **최종 업데이트**: 2025.11.07 16:30
- **기술 스택**: Java, Spring Boot, Python, FastAPI, Kafka, PostgreSQL, Redis, Docker, Debezium

## 구현 진행 상황

### Phase 0: 인프라 및 개발 환경 구축
```
인프라 환경 구축 계획 수립 - 2025.11.07 16:30
```

**구현 내용**:
- Docker Compose 기반 Kafka 클러스터 구성
  - Zookeeper 3노드 클러스터
  - Kafka 브로커 3노드 구성
  - Kafka Connect 및 Schema Registry 설정
- PostgreSQL 데이터베이스 구성
  - 메인 DB (Service-B Ingestor용)
  - AI DB (Service-C AI/ML Server용)
- Redis 캐시 서버 구성
  - 실시간 특징 저장소
  - 세션 관리 및 임시 데이터 저장
- Debezium CDC 커넥터 설정
  - PostgreSQL WAL 레벨 설정 (logical)
  - Outbox 테이블 모니터링 설정

### Phase 1: [Service-A] Data Simulator 활용
```
Data Simulator 연동 계획 수립 - 2025.11.07 16:30
```

**기존 구현 활용**:
- 위치: `/Users/hyuk/PycharmProjects/data-simulator/`
- 기존 Dynamic API Simulator를 활용하여 센서 데이터 생성
- AI4I 2020 데이터셋 패턴 기반 시뮬레이션 추가

**필요 수정사항**:
1. Kafka Producer 추가
   - `sensor-raw-data` 토픽으로 데이터 발행
   - JSON 메시지 포맷 준수
2. 시뮬레이션 프로필 추가
   - normal: 정상 운영 상태
   - overstrain: 과부하 상태
   - heat_dissipation: 열 발산 이상

### Phase 2: [Service-B] Ingestor 구현
```
Ingestor 서비스 구현 계획 수립 - 2025.11.07 16:30
```

**구현 내용**:
1. **Spring Boot 프로젝트 구조**
   ```
   ingestor/
   ├── src/main/java/com/smartfactory/ingestor/
   │   ├── config/
   │   │   └── KafkaConfig.java
   │   ├── consumer/
   │   │   └── SensorDataConsumer.java
   │   ├── entity/
   │   │   └── SensorLog.java
   │   ├── repository/
   │   │   └── SensorLogRepository.java
   │   └── service/
   │       └── DataIngestionService.java
   ```

2. **Kafka Consumer 구현**
   ```java
   @KafkaListener(topics = "sensor-raw-data",
                  groupId = "ingestor-group")
   public void consume(String message) {
       // JSON 파싱 및 DB 저장
   }
   ```

3. **데이터베이스 스키마**
   ```sql
   CREATE TABLE sensor_log (
       id BIGSERIAL PRIMARY KEY,
       machine_id VARCHAR(50) NOT NULL,
       timestamp TIMESTAMP NOT NULL,
       air_temp_k DECIMAL(10,2),
       process_temp_k DECIMAL(10,2),
       rpm INTEGER,
       torque_nm DECIMAL(10,2),
       tool_wear_min INTEGER,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

### Phase 3: [Service-C] AI/ML Server 구현
```
AI/ML Server 구현 계획 수립 - 2025.11.07 16:30
```

**구현 내용**:
1. **FastAPI 프로젝트 구조**
   ```
   ai_ml_server/
   ├── app/
   │   ├── models/
   │   │   ├── anomaly_detector.py
   │   │   └── feature_extractor.py
   │   ├── services/
   │   │   ├── kafka_consumer.py
   │   │   ├── redis_service.py
   │   │   └── outbox_service.py
   │   ├── database/
   │   │   ├── models.py
   │   │   └── connection.py
   │   └── main.py
   ```

2. **실시간 특징 추출 (Redis)**
   ```python
   # Redis 키 구조
   redis_keys = {
       "last_torque": f"machine:{machine_id}:last_torque",
       "temp_1m_avg": f"machine:{machine_id}:temp_1m_avg",
       "rpm_variance": f"machine:{machine_id}:rpm_variance"
   }
   ```

3. **Transactional Outbox Pattern**
   ```python
   async def detect_anomaly(data):
       if is_anomaly:
           async with db.transaction():
               # 1. anomaly_history 저장
               await save_anomaly_history(...)
               # 2. outbox 테이블에 이벤트 저장
               await save_to_outbox(
                   topic="anomaly-alerts",
                   payload=anomaly_data
               )
   ```

4. **모델 학습 API**
   ```python
   @app.post("/train-model")
   async def train_model():
       # PostgreSQL에서 sensor_log 데이터 로드
       # PyTorch 모델 학습
       # 모델 저장 (.pth 파일)
   ```

### Phase 4: [Service-D] Portal API 구현
```
Portal API 서비스 구현 계획 수립 - 2025.11.07 16:30
```

**구현 내용**:
1. **Spring Boot 프로젝트 구조**
   ```
   portal-api/
   ├── src/main/java/com/smartfactory/portal/
   │   ├── controller/
   │   │   ├── MachineController.java
   │   │   └── AlertController.java
   │   ├── service/
   │   │   ├── MachineService.java
   │   │   └── AlertService.java
   │   ├── consumer/
   │   │   └── AnomalyAlertConsumer.java
   │   └── dto/
   │       ├── MachineStatusDTO.java
   │       └── AlertDTO.java
   ```

2. **REST API 엔드포인트**
   ```java
   @RestController
   @RequestMapping("/api")
   public class MachineController {
       @GetMapping("/machines/{machineId}/logs")
       // 센서 로그 조회

       @GetMapping("/machines/{machineId}/status")
       // 현재 상태 조회 (Redis)

       @GetMapping("/alerts")
       // 알람 내역 조회
   }
   ```

3. **알람 처리**
   ```java
   @KafkaListener(topics = "anomaly-alerts")
   public void handleAlert(String message) {
       // 로그 출력
       // 이메일/슬랙 발송
       // WebSocket으로 실시간 전송
   }
   ```

### Phase 5: CDC(Change Data Capture) 및 통합 구성
```
CDC 및 통합 구성 계획 수립 - 2025.11.07 16:30
```

**구현 내용**:
1. **Debezium Connector 설정**
   ```json
   {
     "name": "outbox-connector",
     "config": {
       "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
       "database.hostname": "ai-db",
       "database.port": "5432",
       "database.user": "postgres",
       "database.password": "password",
       "database.dbname": "ai_db",
       "table.include.list": "public.outbox",
       "transforms": "outbox",
       "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter"
     }
   }
   ```

2. **Kafka Topics 구성**
   ```bash
   # 토픽 생성
   kafka-topics --create --topic sensor-raw-data --partitions 6 --replication-factor 3
   kafka-topics --create --topic anomaly-alerts --partitions 3 --replication-factor 3
   ```

### Phase 6: Docker 컨테이너화 및 오케스트레이션
```
Docker 구성 계획 수립 - 2025.11.07 16:30
```

**docker-compose.yml 구조**:
```yaml
version: '3.8'

services:
  # Infrastructure
  zookeeper:
    image: confluentinc/cp-zookeeper:latest

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper

  postgres-main:
    image: postgres
    environment:
      POSTGRES_DB: main_db

  postgres-ai:
    image: postgres
    environment:
      POSTGRES_DB: ai_db
      # WAL 레벨 설정 for CDC
      POSTGRES_INITDB_ARGS: "-c wal_level=logical"

  redis:
    image: redis:alpine

  kafka-connect:
    image: debezium/connect:latest

  # Services
  data-simulator:
    build: ../PycharmProjects/data-simulator
    depends_on:
      - kafka

  ingestor:
    build: ./ingestor
    depends_on:
      - kafka
      - postgres-main

  ai-ml-server:
    build: ./ai-ml-server
    depends_on:
      - kafka
      - postgres-ai
      - redis

  portal-api:
    build: ./portal-api
    depends_on:
      - kafka
      - postgres-main
      - redis
```

## 구현 우선순위 및 로드맵

### 우선순위 매트릭스
```
Priority 1 (필수 - Week 1):
- 인프라 구축 (Docker, Kafka, DB)
- Ingestor 서비스 (데이터 수집)
- 기본 데이터 파이프라인

Priority 2 (핵심 - Week 2):
- AI/ML Server 구현
- 이상 탐지 로직
- Outbox Pattern 구현

Priority 3 (완성 - Week 3):
- Portal API 구현
- CDC 설정 및 통합
- 통합 테스트

Priority 4 (최적화 - Week 4):
- 성능 튜닝
- 모니터링 구축
- 문서화
```

## 리스크 분석 및 완화 전략

### 기술적 리스크
| 리스크 | 영향도 | 확률 | 완화 전략 |
|--------|--------|------|-----------|
| Kafka 메시지 유실 | High | Low | Replication factor 3, ack=all 설정 |
| CDC 지연 | Medium | Medium | Debezium 성능 튜닝, 배치 처리 |
| Redis 메모리 부족 | Medium | Low | TTL 설정, 주기적 클리닝 |
| AI 모델 정확도 저하 | High | Medium | 지속적 재학습, A/B 테스팅 |
| 트랜잭션 정합성 | High | Low | Outbox Pattern 적용 |

### 운영 리스크
| 리스크 | 영향도 | 확률 | 완화 전략 |
|--------|--------|------|-----------|
| 서비스 장애 | High | Low | Health Check, 자동 재시작 |
| 네트워크 단절 | Medium | Low | Retry 메커니즘, Circuit Breaker |
| 리소스 부족 | Medium | Medium | Auto Scaling, 리소스 모니터링 |

## 예상 문제점 및 해결 방안

### 1. Kafka 설정 이슈
```
예상 문제: Bootstrap server 연결 실패
해결 방안:
- Docker 네트워크 설정 확인
- advertised.listeners 설정 검증
- 방화벽 규칙 확인
```

### 2. Database 연결 이슈
```
예상 문제: Connection Pool 고갈
해결 방안:
- HikariCP 설정 최적화
- maximum-pool-size 조정
- connection-timeout 설정
```

### 3. Redis 성능 이슈
```
예상 문제: 대량 데이터 처리 시 지연
해결 방안:
- Pipeline 사용
- Lua Script로 원자적 연산
- Cluster 모드 고려
```

### 4. CDC 동기화 이슈
```
예상 문제: Outbox 테이블 변경 감지 지연
해결 방안:
- WAL 레벨 확인 (logical)
- Debezium 폴링 간격 조정
- 스냅샷 모드 설정
```

## 모니터링 및 알림 체계

### 메트릭 수집
- **Prometheus**: 시스템 메트릭 수집
- **Grafana**: 대시보드 시각화
- **Kafka Manager**: Kafka 클러스터 모니터링

### 로그 관리
- **ELK Stack**: 중앙 집중식 로그 관리
- **Structured Logging**: JSON 형식 로그
- **Log Levels**: ERROR, WARN, INFO, DEBUG

### 알림 설정
- **Critical**: 서비스 다운, 데이터 유실
- **Warning**: 높은 지연, 리소스 80% 이상
- **Info**: 이상 탐지, 정기 리포트

## 테스트 전략

### 단위 테스트
- JUnit (Java)
- pytest (Python)
- 코드 커버리지 80% 이상

### 통합 테스트
- End-to-End 데이터 흐름 테스트
- 이상 탐지 시나리오 테스트
- CDC 동작 검증

### 부하 테스트
- Apache JMeter
- 초당 1000건 처리 목표
- 24시간 연속 실행 테스트

### 카오스 엔지니어링
- 서비스 장애 시뮬레이션
- 네트워크 지연 테스트
- 리소스 제한 테스트

## 완료 기준

### 기능 요구사항
- [ ] 실시간 센서 데이터 수집
- [ ] Kafka 기반 스트리밍 파이프라인
- [ ] AI 기반 이상 탐지
- [ ] Transactional Outbox Pattern
- [ ] CDC를 통한 이벤트 발행
- [ ] REST API 제공
- [ ] 실시간 알림 시스템

### 비기능 요구사항
- [ ] 초당 1000건 처리
- [ ] 99.9% 가용성
- [ ] 데이터 정합성 보장
- [ ] 1초 이내 알림 전달
- [ ] 수평 확장 가능

## 에러 및 해결 과정
```
-- 에러 발생 시 여기에 기록 --
```

---

**프로젝트 상태**: 📝 구현 계획 수립 완료
**마지막 업데이트**: 2025.11.07 16:30
**다음 단계**: Phase 0 - 인프라 구축 시작