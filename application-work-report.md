# 🤖 실시간 스마트 팩토리 예지보전 MSA 프로젝트 구현 진행 보고서

## 프로젝트 개요
- **프로젝트명**: Real-time Smart Factory Predictive Maintenance MSA System
- **구현 계획 수립**: 2025.11.07 16:30
- **최종 업데이트**: 2025.11.07 21:35
- **기술 스택**: Java, Spring Boot, Spring WebFlux, Python, FastAPI, Kafka, PostgreSQL, Redis, Docker, Debezium

## 구현 진행 상황

### Phase 1: [Service-D] Portal API 구현
```
✅ Portal API 기본 구조 구현 완료 - 2025.11.07 21:00
```

**구현 완료 사항**:

1. **프로젝트 구조 설정** ✅
   ```
   portal/
   ├── src/main/java/dev/study/portal/
   │   ├── common/
   │   │   └── exception/
   │   │       ├── BusinessException.java
   │   │       ├── BusinessErrorCode.java
   │   │       ├── ErrorResponse.java
   │   │       ├── GlobalExceptionHandler.java
   │   │       └── machine/
   │   │           ├── MachineErrorCode.java
   │   │           └── MachineNotFoundException.java
   │   ├── controller/
   │   │   └── MachineController.java
   │   ├── dto/
   │   │   └── machine/
   │   │       ├── MachineCreateDto.java
   │   │       ├── MachineModifyDto.java
   │   │       └── MachineResponseDto.java
   │   ├── entity/
   │   │   ├── BaseEntity.java (JPA Auditing)
   │   │   └── machine/
   │   │       ├── Machine.java
   │   │       └── enums/
   │   │           └── Type.java
   │   ├── repository/
   │   │   └── machine/
   │   │       └── MachineRepository.java
   │   └── service/
   │       └── MachineService.java
   ```

2. **Machine 설비 관리 CRUD API 구현** ✅
   ```java
   @RestController
   @RequestMapping("/api/machine")
   public class MachineController {
       @GetMapping                       // 모든 설비 조회
       @GetMapping("/{id}")              // 특정 설비 조회
       @PostMapping                     // 설비 생성
       @PutMapping("/{id}")              // 설비 수정
       @DeleteMapping("/{id}")           // 설비 삭제
   }
   ```

3. **비즈니스 로직 구현** ✅
   - MachineService: 설비 CRUD 비즈니스 로직
   - JPA 더티 체킹을 활용한 효율적인 수정 로직
   - 트랜잭션 관리 (@Transactional)

4. **예외 처리 체계 구현** ✅
   - BusinessException 기반 계층화된 예외 구조
   - GlobalExceptionHandler를 통한 통합 예외 처리
   - 구조화된 에러 응답 (ErrorResponse)
   - HTTP 상태 코드 매핑

5. **테스트 코드 작성** ✅
   - MachineServiceTest: 단위 테스트 (11개 테스트 케이스)
   - Mockito를 활용한 Repository Mock 처리
   - Given-When-Then 패턴 적용
   - 정상 케이스 및 예외 케이스 검증

**기술적 특징**:
- SuperBuilder 패턴 활용 (엔티티 상속 구조)
- JPA Auditing (생성/수정 시간 및 사용자 자동 기록)
- DTO 패턴 적용 (요청/응답 분리)
- RESTful API 설계 원칙 준수

### Phase 2: 이벤트 기반 동적 데이터 수집 시스템
```
계획 수립 - 2025.11.07 21:30
```

**구현 목표**:
DcpConfig 테이블에 데이터 수집 설정이 추가되면 자동으로 해당 API 엔드포인트에서 데이터를 수집하여 Kafka를 통해 처리하는 이벤트 기반 시스템 구축

**구현 완료 사항**:

1. **DcpConfig CRUD 구현** ✅
   - DcpConfigController: REST API 엔드포인트 (/api/dcp-config)
   - DcpConfigService: 비즈니스 로직 (CRUD + 트랜잭션 관리)
   - DcpConfigRepository: JPA Repository
   - DTO 클래스 (DcpConfigCreateDto, DcpConfigModifyDto, DcpConfigResponseDto)
   - 예외 처리 (DcpConfigNotFoundException, DcpConfigErrorCode)
   - 테스트 코드: DcpConfigServiceTest (14개 테스트 케이스)

**구현 예정 사항**:

2. **이벤트 기반 동적 스케줄러 시스템**
   - JPA EntityListener를 통한 DcpConfig 변경 감지
   - ApplicationEvent 발행 (Created, Updated, Deleted)
   - DynamicSchedulerManager: 스케줄러 생명주기 관리
   - 각 DcpConfig별 독립적인 스케줄러 운영

3. **WebFlux 기반 비동기 데이터 수집**
   - WebClient를 이용한 비동기 논블로킹 API 호출
   - Reactive Streams를 통한 백프레셔 처리
   - API 엔드포인트, 메소드, 헤더 동적 설정
   - 응답 데이터 매핑 및 변환 (Mono/Flux)
   - 에러 처리 및 재시도 메커니즘 (Retry, Circuit Breaker)
   - 타임아웃 설정 및 Connection Pool 관리

4. **Kafka Producer 구현**
   - KafkaTemplate 설정
   - sensor-raw-data 토픽으로 데이터 전송
   - 메시지 직렬화 (JSON)
   - 전송 실패시 처리 로직

5. **Kafka Consumer 구현**
   - @KafkaListener 설정
   - sensor-raw-data 토픽 구독
   - 메시지 역직렬화 및 검증
   - 비동기 처리 최적화

6. **MachineSensorData 영속화**
   - 수신된 센서 데이터를 MachineSensorData 엔티티로 변환
   - 배치 처리를 통한 DB 저장 최적화
   - 트랜잭션 관리
   - 중복 데이터 처리 로직

**아키텍처 흐름**:
```
DcpConfig 추가/수정 → JPA Event → Event Publisher → Event Listener
→ Dynamic Scheduler 생성/수정 → WebClient API 호출 (비동기/주기적)
→ Kafka Producer → sensor-raw-data Topic → Kafka Consumer
→ MachineSensorData 영속화
```

**기술 스택**:
- Spring WebFlux: 비동기 논블로킹 웹 프레임워크
- WebClient: 리액티브 HTTP 클라이언트
- Spring Events: 이벤트 기반 아키텍처
- TaskScheduler: 동적 스케줄링
- Apache Kafka: 메시지 브로커
- JPA EntityListener: DB 변경 감지

**예상 구현 사항 상세**:

```java
// DcpConfig 엔티티 수정
@Entity
@EntityListeners(DcpConfigEventListener.class)
public class DcpConfig extends BaseEntity {
    private Machine machine;
    private Integer collectInterval;  // 수집 주기(초)
    private String apiEndpoint;
    private Boolean enabled;
    private LocalDateTime lastCollectedAt;
    private String apiMethod;
    private String apiHeaders;
    private String dataMapping;
}

// WebClient 기반 데이터 수집 서비스
@Service
public class ReactiveDataCollectorService {
    private final WebClient webClient;

    public Mono<Map<String, Object>> collectData(DcpConfig config) {
        return webClient
            .method(HttpMethod.valueOf(config.getApiMethod()))
            .uri(config.getApiEndpoint())
            .headers(headers -> parseHeaders(config.getApiHeaders()))
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofSeconds(10))
            .retry(3)
            .doOnError(error -> log.error("API 호출 실패", error));
    }
}

// 동적 스케줄러 관리
@Component
public class DynamicSchedulerManager {
    Map<Long, ScheduledFuture<?>> schedulerMap
    createScheduler(DcpConfig)
    updateScheduler(DcpConfig)
    removeScheduler(Long configId)
}
```

### Phase 3: CDC(Change Data Capture) 및 통합 구성
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

### Phase 4: [Service-B] AI Server 구현
```
✅ AI Server 디렉토리 구조화 완료 - 2025.11.19
```

**구현 완료 사항**:

1. **AI 모델 개발 및 학습** ✅
   - XGBoost 모델 학습 완료 (Test Accuracy: 98.0%, F1-Score: 0.7436)
   - SMOTE를 통한 클래스 불균형 처리
   - 특징 공학: 5개 파생 특징 생성 (Temp_diff, Power, Tool_wear_rate, Torque_speed_ratio, Temp_toolwear)
   - 모델 아티팩트 저장: final_model_xgboost.pkl, scaler.pkl, label_encoder_type.pkl, feature_names.pkl

2. **AI Server 프로젝트 구조 설정** ✅
   ```
   ai-server/
   ├── .env                           # 환경 변수 설정
   ├── requirements.txt               # 운영 의존성
   └── src/
       ├── __init__.py
       ├── config.py                  # 중앙화된 설정 관리
       ├── preprocessing/             # 전처리 모듈
       │   └── __init__.py
       ├── kafka/                     # Kafka 통신
       │   └── __init__.py
       ├── cache/                     # 캐싱 레이어
       │   └── __init__.py
       ├── db/                        # 데이터베이스 레이어
       │   └── __init__.py
       ├── ml/                        # ML 추론
       │   └── __init__.py
       └── api/                       # REST API
           └── __init__.py
   ```

3. **설정 관리 시스템 구현** ✅
   - `.env`: 모든 환경 변수 정의 (DB: machine_anomaly_ai_server)
   - `config.py`: Pydantic BaseSettings 기반 타입 안전 설정 관리
   - Portal DB 연결 URL 자동 생성 (머신 타입 조회용)
   - AI Server DB 연결 URL 자동 생성 (Outbox 패턴용)
   - 모델 파일 경로 자동 해석 및 검증

**아키텍처 설계 결정** (MSA 표준 패턴):
```
[Data Simulator] → Kafka(sensor-raw-data) → [AI Server]
                                               - 이상 탐지
                                               - Outbox 저장
                                                    ↓
                                        AI Server Debezium CDC
                                                    ↓
                                        Kafka(anomaly-alerts)
                                                    ↓
                                              [Portal]
                                        - Kafka Listener
                                        - FaultHistory 저장
```

**핵심 원칙**:
- ✅ AI Server는 자신의 DB만 관리 (Outbox 테이블)
- ✅ Portal은 자신의 DB만 관리 (FaultHistory 테이블)
- ✅ Kafka를 통한 완전한 서비스 분리
- ✅ 데이터베이스 독립성 보장

**기술 스택**:
- FastAPI: 비동기 웹 프레임워크
- XGBoost: ML 모델
- PostgreSQL: AI Server DB (Outbox), Portal DB (Machine 타입 조회)
- Kafka: 이벤트 스트리밍
- Debezium: CDC (Change Data Capture)
- Pydantic Settings: 타입 안전 설정 관리

**완료된 구현**:

4. ✅ **전처리 모듈 구현** (2025.11.19 완료)
   - `ai-server/src/preprocessing/feature_engineering.py`
   - SensorDataPreprocessor 클래스 구현
   - JSON → NumPy 배열 변환 파이프라인
   - 5개 파생 특징 생성 (학습과 정확히 일치):
     - Temp_diff, Power, Tool_wear_rate, Torque_speed_ratio, Temp_toolwear
   - 특징 순서 검증 (feature_names.pkl 기준)
   - NaN/Inf 값 검증 로직
   - 저장된 LabelEncoder로 머신 타입 인코딩
   - 싱글톤 패턴 적용 (get_preprocessor())

5. ✅ **ML 모델 로더 구현** (2025.11.19 완료)
   - `ai-server/src/ml/model_loader.py`
   - ModelLoader 클래스 구현
   - XGBoost 모델 로딩 (joblib)
   - predict(), predict_proba() 메서드
   - 모델 메타데이터 관리
   - 싱글톤 패턴 적용 (get_model_loader())
   - 강제 재로드 기능 (reload_model())

6. ✅ **예측기 구현** (2025.11.19 완료)
   - `ai-server/src/ml/predictor.py`
   - AnomalyPredictor 클래스 구현
   - end-to-end 예측 파이프라인 (전처리 → 추론 → 결과)
   - predict() 메서드: 단일 예측 + 확률값 옵션
   - predict_batch() 메서드: 배치 예측
   - 오류 처리 및 로깅
   - 싱글톤 패턴 적용 (get_predictor())

7. ✅ **머신 타입 캐시 구현** (2025.11.19 완료)
   - `ai-server/src/cache/machine_cache.py`
   - MachineTypeCache 클래스 구현
   - TTLCache를 이용한 인메모리 캐싱
   - Portal DB (machine_anomaly) 연결
   - SQLAlchemy 2.0 스타일 쿼리
   - 캐시 히트/미스 로깅
   - 캐시 무효화 및 워밍업 기능
   - 싱글톤 패턴 적용 (get_machine_cache())

8. ✅ **Outbox 패턴 구현** (2025.11.19 완료)
   - `ai-server/src/db/models.py`
   - Outbox 모델: SQLAlchemy 2.0 Declarative Base
   - aggregate_id, event_type, payload(JSON), processed 필드
   - `create_anomaly_event()` 헬퍼 메서드
   - OutboxRepository: 이벤트 저장 및 조회
   - SQLAlchemy 2.0 스타일 쿼리 (select, scalar)
   - 트랜잭션 관리 및 세션 팩토리
   - 싱글톤 패턴 (get_outbox_repository())

9. ✅ **Kafka Producer 구현** (2025.11.19 완료)
   - `ai-server/src/kafka/producer.py`
   - AlertProducer 클래스 구현
   - JSON 직렬화, GZIP 압축
   - acks='all' (모든 replica 확인)
   - 비동기/동기 전송 지원
   - 콜백 기반 성공/실패 처리
   - 메트릭 조회 기능
   - 싱글톤 패턴 (get_alert_producer())

10. ✅ **Kafka Consumer 구현** (2025.11.19 완료)
    - `ai-server/src/kafka/consumer.py`
    - SensorDataConsumer 클래스 구현
    - sensor-raw-data 토픽 구독
    - 실시간 예측 파이프라인 통합:
      1. 메시지 수신 → 2. 머신 타입 조회 (캐시 자동 DB fallback)
      3. 이상 탐지 → 4. Outbox 저장
    - SIGINT/SIGTERM 안전 종료
    - 자동 오프셋 커밋
    - 메트릭 조회 기능
    - run_consumer() 실행 함수

11. ✅ **FastAPI 엔드포인트 구현** (2025.11.19 완료)
    - `ai-server/src/api/routers.py`
    - Pydantic 모델 정의 (Request/Response)
    - `/health`: 헬스체크 (캐시/모델 상태)
    - `/predict`: 수동 예측 (테스트용)
    - `/system/info`: 시스템 정보 조회
    - `/cache/stats`: 캐시 통계
    - HTTPException 기반 오류 처리
    - Swagger/ReDoc 자동 생성

12. ✅ **메인 애플리케이션 구현** (2025.11.19 완료)
    - `ai-server/main.py`
    - lifespan 이벤트 (startup/shutdown 대체)
    - 설정 검증 (validate_settings())
    - DB 테이블 자동 생성
    - ML 모델 사전 로딩
    - 리소스 안전 종료
    - Uvicorn 통합
    - `ai-server/run_consumer.py`: Consumer 전용 실행 스크립트

13. ✅ **문서화** (2025.11.19 완료)
    - `ai-server/README.md`
    - 아키텍처 다이어그램
    - 설치/실행 가이드
    - API 엔드포인트 문서
    - 운영 가이드
    - 트러블슈팅

**구현 예정 사항**:

14. ✅ **ai-model 디렉토리 정리** (2025.11.19 완료)
    - 사용하지 않는 파일 삭제:
      - `main.py` (테스트용 Hello World)
      - `fastapi_server.py` (구버전 서버, ai-server로 대체됨)
      - `fix_xgboost_feature_names.py` (일회성 스크립트)
      - `AIModule-Plan.md` (계획 문서, 완료됨)
      - `TROUBLESHOOTING.md` (ai-server README에 통합)
      - `requirements-server.txt` (ai-server로 이동)
      - `setup.sh` (설정 스크립트)
      - `catboost_info/` (CatBoost 학습 로그)
    - README.md 업데이트 (ai-model/ai-server 분리 반영)
    - 깔끔한 ML 개발 영역 구조화 완료

## 에러 및 해결 과정

### 1. XGBoost OpenMP 라이브러리 오류
```
증상: Library not loaded: @rpath/libomp.dylib
해결: brew install libomp 후 XGBoost 재설치
```

### 2. XGBoost Feature Names 오류
```
증상: feature_names must be string, and may not contain [, ] or <
원인: CSV 컬럼명에 특수문자 포함 (Air temperature [K])
해결: 컬럼명 변경 (Air_temperature_K)
```

---

**프로젝트 상태**: 🚀 Phase 4 진행 중 (AI Server)
**마지막 업데이트**: 2025.11.19
**완료 사항**:
- ✅ Portal API 프로젝트 구조 설정
- ✅ Machine 설비 관리 CRUD API 구현
- ✅ 예외 처리 체계 구축
- ✅ 단위 테스트 작성 (MachineServiceTest - 11개)
- ✅ DcpConfig CRUD API 구현 (DcpConfigServiceTest - 14개)
- ✅ AI 모델 개발 및 학습 (XGBoost, Accuracy 98%)
- ✅ AI Server 디렉토리 구조화
- ✅ 설정 관리 시스템 구현
- ✅ AI Server 핵심 컴포넌트 구현 (2025.11.19)
  - ✅ SensorDataPreprocessor (특징 공학)
  - ✅ ModelLoader (ML 모델 로딩)
  - ✅ AnomalyPredictor (예측 파이프라인)
  - ✅ MachineTypeCache (PostgreSQL + TTL)
- ✅ AI Server 완전 구현 완료 (2025.11.19) ✨
  - ✅ Kafka Consumer/Producer
  - ✅ Outbox 패턴 및 DB 모델
  - ✅ FastAPI 엔드포인트
  - ✅ 메인 애플리케이션 및 문서화

**진행 예정**:
- 📋 Portal Kafka Listener (anomaly-alerts 구독)
- 📋 Portal FaultHistory 영속화
- 📋 이벤트 기반 동적 스케줄러 시스템 (Portal)
- 📋 WebFlux/WebClient 기반 비동기 데이터 수집 (Portal)
- 📋 MachineSensorData 영속화 (Portal)
- 📋 Debezium CDC 설정 및 통합
- 📋 Docker Compose 통합 환경 구축