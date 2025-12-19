# GPT-OSS 20B LLM 챗봇 구현 Work Report

---

## 0. 작성 규칙

> 본 문서의 모든 구현 작업은 아래 규칙을 준수해야 합니다.

### 0.1 구현 규칙

| 규칙 번호 | 규칙 내용                                                                                                                                                                      |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Rule 1** | 반드시 현재 실제 시간을 기록하여 구현 시간 및 작성 시간을 기록할 것, step별로 구현이 완료되면 바로 Work-Report에 기록할 것,구현하기 전 시작시간을 먼저 구하여 시작 시간을 work-report에 기록하고 시작할 것 (bash의 `date` 명령 등 시간 함수를 사용하여 실제 시간 확보) |
| **Rule 2** | 모든 주석 및 로그에 이모지를 쓰는 것을 엄중히 금함                                                                                                                                              |
| **Rule 3** | 구현 시 반드시 Context7 MCP를 사용하여 Deprecated된 문법이나 라이브러리, 프레임워크 및 메서드를 개발한 개발자의 의도대로 사용할 것 (안티패턴 금지)                                                                             |
| **Rule 4** | 구현을 시작할 때 "구현 규칙을 준수하여 구현을 시작합니다."라는 말을 시작으로 구현 규칙 열거하고 시작할 것                                                                                                              |
| **Rule 5** | Step별 개발 도중 추가해야 할 사항이나 변경해야 할 사항이 있다면 기존 Step 항목에서 변경 및 추가할 것                                                                                                             |

### 0.2 구현 시작 선언문 (템플릿)

```
구현 규칙을 준수하여 구현을 시작합니다.

- Rule 1: 현재 시간 기록 (작업 시작: YYYY-MM-DD HH:mm)
- Rule 2: 주석 및 로그에 이모지 사용 금지
- Rule 3: Context7 MCP를 통한 공식 문서 확인 후 구현
- Rule 4: 본 선언문으로 구현 시작
- Rule 5: 변경/추가 사항 발생 시 기존 Step에서 수정
```

### 0.3 작업 기록 형식

```markdown
### [Phase X] Task Name
- 작업 시작: YYYY-MM-DD HH:mm
- 작업 완료: YYYY-MM-DD HH:mm
- 소요 시간: X시간 Y분
- 상태: [대기/진행중/완료/보류]
- Context7 참조: [참조한 라이브러리/프레임워크 문서]
- 변경 이력: (Rule 5 적용 시 기록)
  - YYYY-MM-DD HH:mm: [변경 내용]
```

---

## 1. 구현 개요

### 1.1 프로젝트 목표

스마트 팩토리 예지보전 시스템의 설비(Machine) 이상 탐지 시, 운영자가 대화형 인터페이스를 통해 원인을 질의하고, AI가 실시간 센서 데이터(MachineSensorData)와 기술 문서(RAG)를 종합하여 원인 및 조치 사항을 제안하는 시스템 구축.

### 1.2 핵심 기술 스택

| 구분 | 기술 |
|------|------|
| LLM 모델 | GPT-OSS 20B (Quantized: MXFP4/GGUF Q4_K_M) |
| LLM 런타임 | Ollama / vLLM |
| Backend | AI Server (FastAPI, Python 3.11) |
| Cache | Redis |
| Message Broker | Apache Kafka |
| Database | PostgreSQL (AI Server DB: `machine_anomaly_ai_server`) |
| Vector DB | (Phase 3에서 선정) |
| Frontend | React 18 + TypeScript |

### 1.3 변경되는 아키텍처 요약

```
[기존]
AI Server -> Kafka -> Portal (저장+SSE) -> Frontend

[변경 후]
AI Server -> AI Server DB (저장)
          -> Kafka -> Portal (SSE만) -> Frontend

Portal (설비 CUD) -> Redis
AI Server (LLM Tool) -> Redis (설비 조회)
                    -> AI Server DB (이상 탐지 이력 조회)
```

---

## 2. 필요한 기능 및 아키텍처

### 2.1 Phase 0: 아키텍처 변경에서 필요한 기능

#### 2.1.1 AI Server 측

| 기능 | 설명 | 관련 파일 |
|------|------|-----------|
| AnomalyHistory 테이블 생성 | AI Server DB에 이상 탐지 이력 저장 테이블 | `ai-server/src/db/models.py` |
| AnomalyHistory 저장 로직 | 이상 탐지 시 AI Server DB에 직접 저장 | `ai-server/src/kafka/consumer.py` |
| AnomalyHistory API | `GET /anomaly-histories`, `GET /anomaly-histories/search` | `ai-server/src/api/anomaly.py` (신규) |
| Redis 연동 | `machine:{name}` 키로 `machineId` 조회 | `ai-server/src/cache/redis_client.py` (신규) |

#### 2.1.2 Portal 측

| 기능 | 설명 | 관련 파일 |
|------|------|-----------|
| Redis 연동 | 설비 CUD 시 Redis에 `machine:{name}` -> `{id}` 저장 | `portal/src/.../service/MachineService.java` |
| AnomalyAlertListener 변경 | 저장 로직 제거, SSE 브로드캐스트만 수행 | `portal/src/.../listener/AnomalyAlertListener.java` |
| AnomalyHistory 관련 코드 제거 | Entity, Repository, Service, Controller 제거 또는 비활성화 | 다수 파일 |

#### 2.1.3 Frontend 측

| 기능 | 설명 | 관련 파일 |
|------|------|-----------|
| API 엔드포인트 변경 | 이상 탐지 이력 조회를 AI Server로 변경 | `frontend/src/api/anomalies.ts` |

### 2.2 Phase 1: LLM 환경 구축에서 필요한 기능

| 기능 | 설명 |
|------|------|
| Docker 환경 구성 | LLM 전용 Docker 컨테이너 (GPU 지원) |
| 모델 다운로드 | GPT-OSS 20B Quantized 버전 |
| VRAM 최적화 | 모델 로드 테스트 및 메모리 점유율 확인 |

### 2.3 Phase 2: LLM Tool 개발에서 필요한 기능

| 기능 | 설명 |
|------|------|
| `get_machine_id(machine_name)` | Redis에서 설비 ID 조회 |
| `get_anomaly_status(machineId)` | AI Server DB에서 이상 탐지 이력 + sensor_data 조회 |
| 통계 처리 로직 | 센서 데이터 평균 대비 변동률 계산 |
| Function Schema 정의 | LLM이 호출할 수 있는 Tool 스키마 |

### 2.4 Phase 3: RAG 파이프라인에서 필요한 기능

| 기능 | 설명 |
|------|------|
| Vector DB 구축 | 설비 매뉴얼 및 이상 탐지 이력 임베딩 |
| 검색 로직 | Tool 결과 기반 2-Step Retrieval |
| 임베딩 모델 선정 | 한국어 지원 임베딩 모델 |

### 2.5 Phase 4: 통합 및 튜닝에서 필요한 기능

| 기능 | 설명 |
|------|------|
| System Prompt 튜닝 | Few-shot Prompting 적용 |
| Context Window 관리 | 토큰 제한 및 대화 요약 로직 |
| Frontend 챗봇 UI | 채팅 인터페이스 컴포넌트 |

---

## 3. 구현 Step (세부 작업)

### Phase 0: 아키텍처 변경 (선행 작업)

#### Step 0-1: AI Server DB에 AnomalyHistory 테이블 생성

- **작업 내용**:
  - `machine_anomaly_ai_server` DB에 `anomaly_history` 테이블 생성
  - 컬럼: `id`, `machine_id`, `detected_at`, `anomaly_probability`, `sensor_data` (JSONB), `severity`
- **산출물**: DDL 스크립트, SQLAlchemy 모델
- **의존성**: 없음
- **Context7 참조**: SQLAlchemy, PostgreSQL
- **변경 이력**: -

#### Step 0-2: AI Server Consumer에서 AnomalyHistory 저장 로직 추가

- **작업 내용**:
  - `SensorDataConsumer`에서 이상 탐지 시 AI Server DB에 저장
  - 기존 Outbox 패턴 유지 또는 직접 저장으로 변경
- **산출물**: 수정된 `consumer.py`
- **의존성**: Step 0-1
- **Context7 참조**: SQLAlchemy, kafka-python
- **변경 이력**:
  - 2025-12-05 00:01: Outbox 패턴 제거 (옵션 B 선택)
    - Outbox 저장 로직 제거, 직접 Kafka 발행으로 변경
    - `consumer.py`: Outbox import 및 outbox_repository 의존성 제거
    - `consumer.py`: `_handle_anomaly` 메서드에서 Outbox 저장 로직 제거, alert_payload 직접 생성
    - `main.py`: Outbox 관련 초기화 및 종료 로직 제거
    - 이유: 직접 Kafka 발행이 이미 구현되어 있어 CDC 불필요, 아키텍처 단순화

#### Step 0-3: AI Server에 AnomalyHistory API 추가

- **작업 내용**:
  - `GET /anomaly-histories` - 전체 조회
  - `GET /anomaly-histories/search?start=&end=` - 기간별 조회
  - Response DTO 정의
- **산출물**: `ai-server/src/api/anomaly.py`, Pydantic 모델
- **의존성**: Step 0-1, Step 0-2
- **Context7 참조**: FastAPI, Pydantic
- **변경 이력**: -

#### Step 0-4: Portal에 Redis 연동 추가

- **작업 내용**:
  - Spring Data Redis 의존성 추가
  - `MachineEventHandler`에서 설비 CUD 이벤트 수신 시 Redis 저장/삭제 로직 추가
  - Redis 양방향 매핑:
    - `machine:name:{name}` -> `{machineId}` (설비 이름으로 ID 조회용)
    - `machine:id:{machineId}` -> `{machineName}` (ID로 이름 조회용, 수정/삭제 시 이전 키 삭제에 사용)
  - 설비 생성: 양방향 매핑 추가
  - 설비 수정: 이름 변경 시에만 Redis 업데이트 (최적화)
  - 설비 삭제: 양방향 매핑 모두 삭제
- **산출물**: 수정된 `MachineEventHandler.java`
- **의존성**: 없음
- **Context7 참조**: Spring Data Redis
- **변경 이력**:
  - 2025-12-05 16:02: Redis 양방향 매핑 구현 완료 (사용자 직접 구현)
    - `MachineEventHandler.java`: handleCreated, handleModified, handleDeleted 이벤트 핸들러 구현
    - 양방향 매핑으로 설비 이름 변경 시 기존 키 삭제 문제 해결
    - 이름 변경 없는 수정 이벤트 시 Redis 업데이트 스킵 최적화 적용

#### Step 0-5: Portal AnomalyAlertListener 변경

- **작업 내용**:
  - 저장 로직 제거 (AnomalyHistoryService 호출 제거)
  - SSE 브로드캐스트만 수행
  - MachineRepository 의존성으로 변경 (SSE 메시지에 machineName 포함용)
- **산출물**: 수정된 `AnomalyAlertListener.java`
- **의존성**: Step 0-2 (AI Server에서 저장하므로)
- **Context7 참조**: Spring Kafka
- **변경 이력**:
  - 2025-12-05 16:24: AnomalyAlertListener 변경 완료
    - AnomalyHistoryService 의존성 제거
    - SSE 브로드캐스트 전용으로 변경
    - 이상 탐지 이력 저장 로직 제거 (AI Server에서 처리)

#### Step 0-6: Portal AnomalyHistory 관련 코드 정리

- **작업 내용**:
  - `AnomalyHistory` Entity, Repository, Service, Controller 제거
  - `Severity` Enum 제거
  - `AnomalyHistoryResponseDto` 제거
  - Portal DB `anomaly_history` 테이블 삭제 (사용자 직접 수행)
- **산출물**: 정리된 Portal 코드
- **의존성**: Step 0-5
- **Context7 참조**: Spring Data JPA
- **변경 이력**:
  - 2025-12-05 16:24: AnomalyHistory 관련 코드 제거 완료
    - 삭제된 파일: `AnomalyHistory.java`, `Severity.java`, `AnomalyHistoryRepository.java`, `AnomalyHistoryService.java`, `AnomalyHistoryController.java`, `AnomalyHistoryResponseDto.java`
    - 삭제된 디렉토리: `entity/anomalyHistory`, `repository/anomalyHistory`
    - Portal DB 테이블 삭제는 사용자가 직접 수행

#### Step 0-7: AI Server에 Redis Client 추가

- **작업 내용**:
  - `redis-py` 라이브러리 추가
  - `get_machine_id(machine_name)` 함수 구현
- **산출물**: `ai-server/src/cache/redis_client.py`
- **의존성**: Step 0-4
- **Context7 참조**: redis-py
- **변경 이력**:
  - 2025-12-20 01:39: Redis Client 구현 완료
    - `requirements.txt`에 `redis>=5.0.0` 추가
    - `config.py`에 Redis 설정 추가 (REDIS_HOST, REDIS_PORT, REDIS_DB, REDIS_MAX_CONNECTIONS 등)
    - `redis_client.py` 생성: MachineIdCache 클래스, get_machine_id() 함수
    - `cache/__init__.py` 업데이트
    - `.env_ai_server`에 Redis 환경 변수 추가

#### Step 0-8: Frontend API 엔드포인트 변경

- **작업 내용**:
  - `anomalies.ts`의 API 호출을 AI Server로 변경
  - Base URL 및 엔드포인트 수정
- **산출물**: 수정된 `frontend/src/api/anomalies.ts`
- **의존성**: Step 0-3
- **Context7 참조**: Axios
- **변경 이력**:
  - 2025-12-20 01:42: Frontend API 엔드포인트 변경 완료
    - `aiClient.ts` 생성: AI Server 전용 Axios 클라이언트
    - `anomalies.ts` 수정: apiClient -> aiClient로 변경, 엔드포인트 경로 수정
    - `vite.config.ts` 수정: `/ai-api` 프록시 추가 (개발 환경용)
    - `.env_frontend` 수정: VITE_AI_SERVER_URL 환경 변수 추가

#### Step 0-9: Docker Compose 업데이트

- **작업 내용**:
  - Redis 서비스 추가 (없는 경우)
  - 환경 변수 업데이트
- **산출물**: 수정된 `docker-compose.yml`, `.env` 파일
- **의존성**: Step 0-4, Step 0-7
- **Context7 참조**: Docker Compose
- **변경 이력**:
  - 2025-12-20 01:46: Docker Compose 업데이트 완료
    - `docker-compose.yml`: Redis 서비스 추가 (redis:7-alpine, healthcheck 포함)
    - `docker-compose.yml`: Portal, AI Server API, AI Server Consumer에 Redis 의존성 추가
    - `docker-compose.yml`: redis_data 볼륨 추가

---

### Phase 1: LLM 환경 구축

#### Step 1-1: LLM Docker 환경 구성

- **작업 내용**:
  - Ollama 또는 vLLM Docker 이미지 준비
  - GPU 지원 설정 (NVIDIA Container Toolkit)
  - docker-compose에 LLM 서비스 추가
- **산출물**: LLM Dockerfile, docker-compose 업데이트
- **의존성**: 없음
- **Context7 참조**: Ollama, vLLM, Docker
- **변경 이력**: -

#### Step 1-2: GPT-OSS 20B 모델 다운로드 및 설정

- **작업 내용**:
  - Quantized 모델 다운로드 (GGUF Q4_K_M 또는 MXFP4)
  - 모델 설정 파일 작성
- **산출물**: 모델 파일, 설정 파일
- **의존성**: Step 1-1
- **Context7 참조**: Hugging Face, Ollama
- **변경 이력**: -

#### Step 1-3: VRAM 점유율 테스트

- **작업 내용**:
  - 모델 로드 테스트
  - VRAM 사용량 모니터링 (목표: 12~13GB)
  - Context Window 크기별 메모리 테스트
- **산출물**: 테스트 결과 보고서
- **의존성**: Step 1-2
- **Context7 참조**: NVIDIA SMI
- **변경 이력**: -

---

### Phase 2: LLM Tool 개발

#### Step 2-1: Function Schema 정의

- **작업 내용**:
  - `get_machine_id(machine_name: str) -> int` 스키마
  - `get_anomaly_status(machineId: int) -> AnomalyStatusResponse` 스키마
  - OpenAI Function Calling 형식으로 정의
- **산출물**: Function Schema JSON
- **의존성**: Phase 0 완료
- **Context7 참조**: OpenAI Function Calling
- **변경 이력**: -

#### Step 2-2: get_machine_id Tool 구현

- **작업 내용**:
  - Redis에서 `machine:{name}` 키로 `machineId` 조회
  - 존재하지 않는 경우 에러 처리
- **산출물**: Tool 함수 코드
- **의존성**: Step 0-7, Step 2-1
- **Context7 참조**: redis-py
- **변경 이력**: -

#### Step 2-3: get_anomaly_status Tool 구현

- **작업 내용**:
  - AI Server DB에서 machineId로 최근 이상 탐지 이력 조회
  - sensor_data JSON 파싱
  - 통계 처리 (평균 대비 변동률 계산)
  - LLM에게 전달할 JSON 포맷으로 변환
- **산출물**: Tool 함수 코드
- **의존성**: Step 0-3, Step 2-1
- **Context7 참조**: SQLAlchemy, Pydantic
- **변경 이력**: -

#### Step 2-4: 통계 처리 로직 구현

- **작업 내용**:
  - 센서별 정상 범위 정의 (설비 타입별)
  - 평균 대비 변동률 계산
  - anomalies_detected 배열 생성
- **산출물**: 통계 처리 모듈
- **의존성**: Step 2-3
- **Context7 참조**: NumPy, Pandas
- **변경 이력**: -

#### Step 2-5: LLM-Tool 연동 테스트

- **작업 내용**:
  - Tool Call 테스트
  - 실제 이상 탐지 데이터로 End-to-End 테스트
- **산출물**: 테스트 결과
- **의존성**: Step 2-2, Step 2-3, Step 2-4
- **Context7 참조**: Ollama API, vLLM API
- **변경 이력**: -

---

### Phase 3: RAG 파이프라인 구축

#### Step 3-1: Vector DB 선정 및 구축

- **작업 내용**:
  - Vector DB 선정 (ChromaDB, Milvus, Weaviate 등)
  - Docker 환경에 Vector DB 추가
- **산출물**: Vector DB 설정
- **의존성**: 없음
- **Context7 참조**: 선정된 Vector DB 문서
- **변경 이력**: -

#### Step 3-2: 임베딩 모델 선정

- **작업 내용**:
  - 한국어 지원 임베딩 모델 선정
  - 모델 로드 및 테스트
- **산출물**: 임베딩 모델 설정
- **의존성**: Step 3-1
- **Context7 참조**: Sentence Transformers
- **변경 이력**: -

#### Step 3-3: 설비 매뉴얼 임베딩

- **작업 내용**:
  - 설비 매뉴얼 문서 수집
  - 청킹 및 임베딩
  - Vector DB에 저장
- **산출물**: 임베딩된 매뉴얼 데이터
- **의존성**: Step 3-1, Step 3-2
- **Context7 참조**: LangChain
- **변경 이력**: -

#### Step 3-4: 이상 탐지 이력 임베딩

- **작업 내용**:
  - 과거 이상 탐지 이력 데이터 임베딩
  - 정기적 업데이트 로직 구현
- **산출물**: 임베딩된 이상 탐지 이력
- **의존성**: Step 3-1, Step 3-2
- **Context7 참조**: LangChain
- **변경 이력**: -

#### Step 3-5: 2-Step Retrieval 검색 로직 구현

- **작업 내용**:
  - Tool 결과 기반 검색 쿼리 생성
  - Vector DB 검색 및 결과 반환
- **산출물**: 검색 로직 코드
- **의존성**: Step 3-3, Step 3-4
- **Context7 참조**: 선정된 Vector DB 문서
- **변경 이력**: -

---

### Phase 4: 통합 및 튜닝

#### Step 4-1: System Prompt 작성 및 튜닝

- **작업 내용**:
  - 기본 System Prompt 작성
  - Few-shot 예시 추가
  - 테스트 및 조정
- **산출물**: 최종 System Prompt
- **의존성**: Phase 2, Phase 3 완료
- **Context7 참조**: Prompt Engineering 가이드
- **변경 이력**: -

#### Step 4-2: Context Window 관리 로직 구현

- **작업 내용**:
  - 토큰 카운팅 로직
  - 대화 히스토리 요약/삭제 로직
  - 8k~16k 토큰 제한 적용
- **산출물**: Context 관리 모듈
- **의존성**: Phase 2 완료
- **Context7 참조**: tiktoken
- **변경 이력**: -

#### Step 4-3: Frontend 챗봇 UI 개발

- **작업 내용**:
  - 채팅 인터페이스 컴포넌트
  - 메시지 전송/수신 로직
  - "분석 중..." 로딩 상태 표시
- **산출물**: 챗봇 UI 컴포넌트
- **의존성**: Phase 2, Phase 3 완료
- **Context7 참조**: React, shadcn/ui
- **변경 이력**: -

#### Step 4-4: End-to-End 통합 테스트

- **작업 내용**:
  - 전체 플로우 테스트
  - 성능 테스트 (응답 시간)
  - 에러 케이스 테스트
- **산출물**: 테스트 결과 보고서
- **의존성**: Step 4-1, Step 4-2, Step 4-3
- **Context7 참조**: 해당 없음
- **변경 이력**: -

---

## 4. 주의사항

### 4.1 코드 품질

- 모든 주석과 로그 메시지에 이모지 사용 금지
- Context7 MCP를 통해 공식 문서를 확인하여 Deprecated API 사용 금지
- 안티패턴 회피 (예: N+1 쿼리, 순환 의존성 등)

### 4.2 데이터 마이그레이션

- Phase 0 진행 시 기존 Portal DB의 AnomalyHistory 데이터를 AI Server DB로 마이그레이션 필요
- 마이그레이션 스크립트 작성 및 검증 필수

### 4.3 Redis 키 관리

- 설비 이름에 특수문자가 포함될 경우 키 충돌 주의
- 키 네이밍 컨벤션: `machine:{name}` (공백은 허용, 콜론은 구분자로 사용)

### 4.4 VRAM 관리

- 모델 로드 시 VRAM 12~13GB 점유 예상
- Context Window 크기에 따라 추가 3GB 필요
- OOM 발생 시 Context Window 축소 또는 대화 요약 적용

### 4.5 보안

- LLM에게 API 엔드포인트, DB 쿼리 등 내부 시스템 정보 노출 금지
- System Prompt의 Constraints에 명시

### 4.6 테스트

- 각 Step 완료 후 단위 테스트 필수
- Phase 완료 후 통합 테스트 필수
- 기존 테스트 코드와의 호환성 확인

### 4.7 변경 관리 (Rule 5 관련)

- Step 진행 중 변경/추가 사항 발생 시 해당 Step의 "변경 이력"에 기록
- 새로운 Step을 추가하지 않고 기존 Step 내에서 처리
- 변경 사유와 변경 내용을 명확히 기록

---

## 5. 작업 진행 현황

| Phase | Step | 작업 내용 | 상태 | 시작 시간 | 완료 시간 |
|-------|------|-----------|----|-----------|-----------|
| 0 | 0-1 | AI Server DB에 AnomalyHistory 테이블 생성 | 완료 | 2025-12-04 23:00 | 2025-12-04 23:50 |
| 0 | 0-2 | AI Server Consumer 저장 로직 추가 | 완료 | 2025-12-04 23:00 | 2025-12-04 23:50 |
| 0 | 0-3 | AI Server AnomalyHistory API 추가 | 완료 | 2025-12-04 23:00 | 2025-12-04 23:50 |
| 0 | 0-4 | Portal Redis 연동 추가 | 완료 | 2025-12-05 16:02 | 2025-12-05 16:02 |
| 0 | 0-5 | Portal AnomalyAlertListener 변경 | 완료 | - | - |
| 0 | 0-6 | Portal AnomalyHistory 코드 정리 | 완료 | - | - |
| 0 | 0-7 | AI Server Redis Client 추가 | 완료 | 2025-12-20 01:30 | 2025-12-20 01:39 |
| 0 | 0-8 | Frontend API 엔드포인트 변경 | 완료 | 2025-12-20 01:40 | 2025-12-20 01:42 |
| 0 | 0-9 | Docker Compose 업데이트 | 완료 | 2025-12-20 01:43 | 2025-12-20 01:46 |
| 1 | 1-1 | LLM Docker 환경 구성 | 대기 | - | - |
| 1 | 1-2 | GPT-OSS 20B 모델 다운로드 | 대기 | - | - |
| 1 | 1-3 | VRAM 점유율 테스트 | 대기 | - | - |
| 2 | 2-1 | Function Schema 정의 | 대기 | - | - |
| 2 | 2-2 | get_machine_id Tool 구현 | 대기 | - | - |
| 2 | 2-3 | get_anomaly_status Tool 구현 | 대기 | - | - |
| 2 | 2-4 | 통계 처리 로직 구현 | 대기 | - | - |
| 2 | 2-5 | LLM-Tool 연동 테스트 | 대기 | - | - |
| 3 | 3-1 | Vector DB 선정 및 구축 | 대기 | - | - |
| 3 | 3-2 | 임베딩 모델 선정 | 대기 | - | - |
| 3 | 3-3 | 설비 매뉴얼 임베딩 | 대기 | - | - |
| 3 | 3-4 | 이상 탐지 이력 임베딩 | 대기 | - | - |
| 3 | 3-5 | 2-Step Retrieval 검색 로직 구현 | 대기 | - | - |
| 4 | 4-1 | System Prompt 작성 및 튜닝 | 대기 | - | - |
| 4 | 4-2 | Context Window 관리 로직 구현 | 대기 | - | - |
| 4 | 4-3 | Frontend 챗봇 UI 개발 | 대기 | - | - |
| 4 | 4-4 | End-to-End 통합 테스트 | 대기 | - | - |

---

*문서 작성일: 2025-12-04*
*최종 수정일: 2025-12-20*