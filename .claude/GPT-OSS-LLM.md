# GPT-OSS 20B 기반 이상 탐지 분석 챗봇 도입 기획서

---

## 1. 개요 (Overview)

- **프로젝트명**: `Industrial Anomaly Analysis Assistant (IA3)` - (가칭)
- **목표**: 스마트 팩토리 예지보전 시스템의 설비(Machine) 이상 탐지 시, 운영자가 대화형 인터페이스를 통해 원인을 질의하고, AI가 실시간 센서 데이터(MachineSensorData)와 기술 문서(RAG)를 종합하여 원인 및 조치 사항을 제안하는 시스템 구축.
- **핵심 모델**: **OpenAI GPT-OSS 20B** (Open Weights)
- **운영 환경**: 온프레미스 단일 워크스테이션 (NVIDIA RTX 5060 Ti 16GB)
- **연동 시스템**: 기존 예지보전 MSA 시스템 (Portal API, AI Server, Kafka)

---

## 2. 하드웨어 및 모델 선정 근거

### 2.1 하드웨어 스펙

- **GPU**: NVIDIA GeForce RTX 5060 Ti (VRAM 16GB)
- **RAM**: System Memory 32GB 이상 권장 (VRAM 오버헤드 방지)

### 2.2 모델 선정: GPT-OSS 20B

- **선정 이유**:
    - **경량화**: MoE(Mixture of Experts) 구조로 Active Parameter가 약 3.6B에 불과하여 빠른 추론 속도 보장.
    - **비용 효율성**: 16GB VRAM 환경에서 구동 가능한 가장 성능 좋은 최신 모델 중 하나.
    - **기능성**: Function Calling(Tool Use) 및 RAG 추론 능력 보유.
- **배포 전략**:
    - **양자화(Quantization)**: `MXFP4` 또는 `GGUF (Q4_K_M)` 4-bit 양자화 버전 사용 필수.
    - **메모리 점유**: 모델 로드(약 12~13GB) + 컨텍스트 버퍼(약 3GB) 확보.

---

## 3. 시스템 아키텍처 (System Architecture)

### 3.1 워크플로우 (Data Flow)

> 사용자가 질문을 던졌을 때의 처리 흐름은 **"요청 시 분석(On-Demand Analysis)"** 방식을 따름.

1.  **User Query**: `"CNC Machine 1에서 CRITICAL 알림이 떴는데 원인이 뭐야?"`
2.  **Intent Recognition (LLM)**: 사용자 의도를 파악하여 적절한 Custom Tool 호출 결정.
    - `Call: get_machine_id(machine_name="CNC Machine 1")` → Redis에서 `machineId` 조회
    - `Call: get_anomaly_status(machineId=1)`
3.  **Data Retrieval (AI Server)**:
    - AI Server DB에서 해당 설비의 이상 탐지 이력(AnomalyHistory) 조회 (sensor_data JSON 포함).
    - **[중요]** 데이터 전처리: LLM에게 Raw Data를 주지 않고, **통계적 요약(평균 대비 변동률 등)**을 계산하여 JSON 생성.
4.  **Knowledge Retrieval (RAG)**:
    - Tool 결과(예: "torque 수치 급등, severity: CRITICAL")를 키워드로 Vector DB 검색.
    - 관련된 설비 매뉴얼, 과거 이상 탐지 이력(AnomalyHistory) 추출.
5.  **Reasoning & Response (LLM)**:
    - **입력**: `[System Prompt]` + `[Tool Result JSON]` + `[RAG Docs]` + `[User Query]`
    - **출력**: 현상 분석, 추정 원인, 권장 조치를 포함한 자연어 응답 생성.

### 3.2 아키텍처 변경 사항 (선행 작업)

> LLM 챗봇 도입을 위해 기존 MSA 시스템의 일부 아키텍처 변경이 필요함.

#### 3.2.1 AnomalyHistory 테이블 이동

**현재 구조:**
```
AI Server (이상 탐지) → Kafka (anomaly-alerts) → Portal (저장) → Portal DB
```

**변경 후 구조:**
```
AI Server (이상 탐지) → AI Server DB (저장)
                     → Kafka (anomaly-alerts) → Portal (SSE 브로드캐스트만)
```

| 항목 | 현재 | 변경 후 |
|------|------|---------|
| AnomalyHistory 저장 위치 | Portal DB (`machine_anomaly`) | AI Server DB (`machine_anomaly_ai_server`) |
| AnomalyHistory API | Portal (`/api/anomaly-histories`) | AI Server (새 API 추가) |
| SSE 실시간 알림 | Portal (`/api/sse/subscribe`) | Portal 유지 (Kafka 경유) |
| Frontend 이상 이력 조회 | Portal API 호출 | AI Server API 직접 호출 |

> **참고**: AnomalyHistory 테이블에는 `sensor_data` 컬럼이 JSON 형태로 센서 데이터(airTemperature, processTemperature, rotationalSpeed, torque, toolWear)를 포함하고 있어, 이상 탐지 시점의 센서 데이터 조회를 위해 별도 API 호출이 불필요함.

#### 3.2.2 설비 정보 Redis 캐시 도입

**목적**: LLM이 설비 이름(`machine_name`)으로 질의할 때 `machineId`를 빠르게 조회하기 위함.

**현재 구조:**
```
LLM Tool → AI Server → Portal API (HTTP) → Portal DB
```

**변경 후 구조:**
```
Portal (설비 CUD 이벤트) → Redis 저장
LLM Tool → AI Server → Redis (직접 조회)
```

**Redis 키 구조:**
```
Key: machine:{machine_name}
Value: {machineId}
```

**Portal 동작:**

| 이벤트 | Redis 동작 |
|--------|------------|
| 설비 생성 (POST /api/machine) | `SET machine:{name} {id}` |
| 설비 수정 (PUT /api/machine/{id}) | 이름 변경 시: `DEL machine:{old_name}` → `SET machine:{new_name} {id}` |
| 설비 삭제 (DELETE /api/machine/{id}) | `DEL machine:{name}` |

#### 3.2.3 변경 이유

- **LLM Tool 효율성**: AI Server 내에서 직접 이상 탐지 이력 및 설비 정보 조회 가능 (Portal API 경유 불필요)
- **데이터 응집성**: AI 관련 데이터(예측 결과, 이상 탐지 이력)가 AI Server에 응집
- **응답 속도**: Redis 조회로 API 호출 오버헤드 제거, 네트워크 홉 감소

#### 3.2.4 SSE 알림 흐름 (Portal 유지)

```
AI Server (이상 탐지)
    ├─ AI Server DB 저장 (AnomalyHistory)
    └─ Kafka (anomaly-alerts) 발행
           ↓
Portal (AnomalyAlertListener)
    └─ SSE 브로드캐스트 (저장 없이 알림만)
           ↓
Frontend (Toast 알림)
```

---

## 4. 데이터 처리 전략

### 4.1 Custom Tool (AI Server API 연동)

20B 모델의 SQL 생성 오류를 방지하기 위해, SQL 생성 대신 **AI Server API 기반 도구 호출 방식**을 채택.

- **기능**: 특정 시점의 센서 데이터(MachineSensorData) 스냅샷 및 이동평균(Moving Average) 비교, 이상 탐지 이력(AnomalyHistory) 조회.
- **Output JSON 예시** (LLM에게 전달되는 형태):
  ```json
  {
    "machineId": 1,
    "machineName": "CNC Machine 1",
    "machineType": "HIGH",
    "detectedAt": "2025-12-04T14:30:00",
    "anomalyProbability": 0.85,
    "severity": "CRITICAL",
    "sensorData": {
      "airTemperature": 305.2,
      "processTemperature": 315.8,
      "rotationalSpeed": 1800,
      "torque": 55.3,
      "toolWear": 180
    },
    "anomalies_detected": [
      {
        "sensor": "torque",
        "current_value": 55.3,
        "normal_average": 40.2,
        "status": "CRITICAL (+37.6%)"
      },
      {
        "sensor": "processTemperature",
        "current_value": 315.8,
        "normal_average": 308.5,
        "status": "WARNING (+2.4%)"
      }
    ]
  }
  ```

### 4.2 RAG (검색 증강 생성)

- **검색 전략 (2-Step Retrieval)**:
    1.  사용자 질문만으로 검색하지 않음 (정확도 낮음).
    2.  Tool 실행 결과를 바탕으로 검색 수행 (예: `"torque CRITICAL HIGH 타입 설비"` 키워드로 매뉴얼 검색)하여 정확도 향상.

---

## 5. 프롬프트 엔지니어링 (System Prompt)

> GPT-OSS 20B의 성능을 극대화하고 환각(Hallucination)을 억제하기 위한 지침.

#### ### Role
> 당신은 스마트 팩토리 예지보전 시스템의 이상 징후를 분석하는 전문 AI 어시스턴트입니다.

#### ### Instructions
> 1. **데이터 기반 분석**: 반드시 제공된 `[Tool Output]`의 센서 데이터(airTemperature, processTemperature, rotationalSpeed, torque, toolWear) 및 anomalyProbability 수치에 근거하여 현재 상황을 설명하세요.
> 2. **심각도 해석**: severity 값(WARNING: 30%↑, ALERT: 50%↑, CRITICAL: 70%↑)에 따라 긴급도를 판단하세요.
> 3. **원인 추론**: `[Context](RAG)`에 제공된 설비 매뉴얼을 바탕으로, 감지된 이상 수치가 어떤 고장을 시사하는지 연결하세요.
> 4. **간결한 보고**: 답변은 '현상 파악' → '추정 원인' → '권장 조치' 순서로 구조화하여 답변하세요.

#### ### Constraints
> - 모호한 추측을 사실인 것처럼 말하지 마세요.
> - API 엔드포인트나 내부 시스템 코드를 노출하지 마세요.
> - 설비 타입(LOW/MEDIUM/HIGH)에 따른 정상 범위 차이를 고려하세요.

---

## 6. 구현 로드맵 (Roadmap)

- **Phase 0: 아키텍처 변경 (선행 작업)**
    - AnomalyHistory 테이블을 AI Server DB(`machine_anomaly_ai_server`)로 이동.
    - AI Server에 AnomalyHistory API 추가 (`GET /anomaly-histories`, `GET /anomaly-histories/search`).
    - Portal의 AnomalyAlertListener를 SSE 브로드캐스트 전용으로 변경 (저장 로직 제거).
    - Portal에 Redis 연동 추가: 설비 CUD 시 `machine:{name}` → `{id}` 저장.
    - Frontend의 이상 탐지 이력 조회 API를 AI Server로 변경.
- **Phase 1: LLM 환경 구축**
    - Docker 컨테이너 기반 LLM 실행 환경 구성 (`Ollama`/`vLLM`).
    - GPT-OSS 20B (Quantized) 모델 로드 테스트 및 VRAM 점유율 최적화.
- **Phase 2: LLM Tool 개발**
    - AI Server에 LLM용 통계 처리 로직 추가 (센서 데이터 평균 대비 변동률 계산).
    - LLM이 호출할 수 있는 Function Schema 정의:
        - `get_machine_id(machine_name)` → Redis 조회
        - `get_anomaly_status(machineId)` → AI Server DB (AnomalyHistory + sensor_data)
    - 기존 이상 탐지 데이터를 활용한 Tool Call 테스트.
- **Phase 3: RAG 파이프라인 구축**
    - 설비 매뉴얼 및 과거 이상 탐지 이력(AnomalyHistory) 데이터 임베딩 (Vector DB 구축).
    - Tool 결과(severity, sensor 이상 항목)와 연동되는 검색 로직 구현.
- **Phase 4: 통합 및 튜닝**
    - 시스템 프롬프트 미세 조정 (Few-shot Prompting 적용).
    - Context Window(토큰 제한) 관리 로직 최적화.
    - Frontend 챗봇 UI 통합.

---

## 7. 리스크 및 대응 방안

| 리스크 항목 | 상세 내용 | 대응 방안 |
| :--- | :--- | :--- |
| **VRAM 부족 (OOM)** | 대화가 길어지거나 조회 데이터가 많으면 16GB 초과 | Context Window를 8k~16k로 제한하고, 오래된 대화는 요약하거나 삭제. |
| **환각 (Hallucination)** | 없는 데이터를 지어내거나 계산 실수 | 계산은 전적으로 Python 백엔드에 위임하고, LLM은 '해석'만 수행하도록 역할 제한. |
| **응답 속도 저하** | RAG 검색 및 추론 대기 시간 발생 | 사용자에게 "데이터 분석 중..." 상태를 시각적으로 표시하고, Tool 결과 JSON을 경량화. |

---
*작성일: 2025. 12. 04*

---

### 💡 추가 제안 (Next Step)
> 이 기획서를 바탕으로 **"Phase 2: 백엔드 및 툴 개발"** 단계에서 사용할 **API(Tool)의 입출력 명세서**를 먼저 작성해 보세요.
