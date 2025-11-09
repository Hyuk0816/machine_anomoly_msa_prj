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

## 에러 및 해결 과정
```
-- 에러 발생 시 여기에 기록 --
```

---

**프로젝트 상태**: 🚀 Phase 2 진행 중
**마지막 업데이트**: 2025.11.07 (진행중)
**완료 사항**:
- ✅ Portal API 프로젝트 구조 설정
- ✅ Machine 설비 관리 CRUD API 구현
- ✅ 예외 처리 체계 구축
- ✅ 단위 테스트 작성 (MachineServiceTest - 11개)
- ✅ DcpConfig CRUD API 구현 (DcpConfigServiceTest - 14개)
**진행 예정**:
- 🚀 이벤트 기반 동적 스케줄러 시스템
  - JPA EntityListener 구현
  - ApplicationEvent 발행/구독 시스템
  - DynamicSchedulerManager 구현
- 📋 WebFlux/WebClient 기반 비동기 데이터 수집
- 📋 Kafka Producer/Consumer 구현
- 📋 MachineSensorData 영속화