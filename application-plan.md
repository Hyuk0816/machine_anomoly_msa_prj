# 🤖 [포트폴리오] 실시간 스마트 팩토리 예지보전 MSA 프로젝트 기획서 (LLM 협업용)

> **프로젝트 목표:** MSA(Microservice Architecture) 환경에서 Kafka, AI/ML, CDC(Change Data Capture) 패턴을 활용하여 스마트 팩토리 설비의 이상 징후를 실시간으로 탐지하고 알림을 제공하는 예지보전(PdM) 시스템을 구축합니다.

-----

## 1\. 시스템 아키텍처 (MSA)

본 시스템은 4개의 핵심 마이크로서비스와 데이터 인프라로 구성됩니다.

1.  **[Service-A] Data Simulator (Python, FastAPI):** 가상 설비. 실시간 센서 데이터를 생성하여 Kafka로 발행(Produce)합니다.
2.  **[Service-B] Ingestor (Java, Spring Boot):** 데이터 수집기. Kafka의 원천 데이터를 구독(Consume)하여 메인 RDB에 적재합니다.
3.  **[Service-C] AI/ML Server (Python, FastAPI/Torch):** 분석 엔진. 데이터를 학습/추론하여 이상 징후를 감지하고, 그 결과를 Outbox 패턴을 통해 발행합니다.
4.  **[Service-D] Portal API (Java, Spring Boot):** 사용자 포털. 대시보드 API를 제공하고, AI가 감지한 알람을 구독하여 처리합니다.

-----

## 2\. 데이터 흐름 (Data Pipeline)

1.  **(A) Simulator**가 `sensor-raw-data` 토픽 (Kafka)으로 센서 데이터(JSON)를 발행합니다.
2.  **(B) Ingestor**가 `sensor-raw-data`를 구독하여 PostgreSQL의 `sensor_log` 테이블에 적재합니다.
3.  **(C) AI/ML Server**도 `sensor-raw-data`를 실시간 구독합니다.
      * **실시간 특징 추출:** Redis에 "최근 1분 평균 온도" 등을 저장합니다. (e.g., `machine:A:temp_1m_avg`)
      * **추론:** 학습된 모델과 Redis 특징을 조합하여 이상 징후를 판단합니다.
4.  이상 징후 감지 시, **(C) AI/ML Server**는 **자신의 로컬 DB `outbox` 테이블**에 이벤트(알람 내용)를 `INSERT`합니다.
5.  \*\*Debezium (CDC)\*\*이 `outbox` 테이블의 변경을 감지하여 `anomaly-alerts` 토픽 (Kafka)으로 메시지를 발행합니다.
6.  **(D) Portal API**가 `anomaly-alerts` 토픽을 구독하여 실시간 알림(로그 출력, 이메일 전송 등)을 처리합니다.

-----

## 3\. 핵심 기술 스택

  * **Backend:** Java 17, Spring Boot 3.x, Python 3.10, FastAPI, Torch, Pandas, PySpark
  * **Data Streaming:** Kafka, Kafka Connect, Debezium (for CDC)
  * **Database:** PostgreSQL (메인 DB, AI 서버 DB), Redis (실시간 특징 저장소)
  * **Infra:** Docker Compose

-----

## 4\. 서비스별 상세 명세 (LLM 협업용)

### 📦 [Service-A] Data Simulator (FastAPI)

  * **역할:** "설비" 그 자체. `AI4I 2020` 데이터셋의 통계 패턴을 기반으로 가상 센서 데이터를 생성합니다.
  * **데이터셋:** [Kaggle: AI4I 2020 Predictive Maintenance Dataset](https://www.kaggle.com/datasets/shivamb/machine-predictive-maintenance-classification)
  * **API Endpoints:**
      * `POST /simulate/{machine_id}`
      * **Query Params:** `profile` (normal, overstrain, heat\_dissipation), `duration_sec` (int)
      * **동작:**
        1.  `machine_id`별로 백그라운드 태스크(asyncio)를 실행합니다.
        2.  `profile`에 맞는 통계 분포(정규분포 등)를 사용하여 가상 데이터를 생성합니다.
              * `normal`: 정상 범위 (예: Torque 30-50 Nm)
              * `overstrain`: 이상 징후 (예: Torque 70-90 Nm, Tool Wear 급증)
        3.  생성된 데이터를 JSON 형식으로 Kafka `sensor-raw-data` 토픽에 발행합니다.
  * **Kafka Message Format (`sensor-raw-data`):**
    ```json
    {
      "machine_id": "machine-001",
      "timestamp": "2025-11-07T15:10:01Z",
      "air_temp_k": 299.1,
      "process_temp_k": 309.5,
      "rpm": 1500,
      "torque_nm": 42.8,
      "tool_wear_min": 5
    }
    ```

### 💾 [Service-B] Ingestor (Spring Boot)

  * **역할:** Kafka의 원천 데이터를 RDB에 영구 저장합니다. (AI 학습 데이터셋 구축)
  * **Kafka Consumer:**
      * `@KafkaListener(topics = "sensor-raw-data")`
      * 수신한 JSON을 `SensorLog` DTO/Entity로 변환합니다.
  * **Database (PostgreSQL - Main DB):**
      * `sensor_log` 테이블: `machine_id`, `timestamp`, `air_temp_k`, `process_temp_k`, `rpm`, `torque_nm`, `tool_wear_min`

### 🤖 [Service-C] AI/ML Server (Python)

  * **역할:** 실시간 이상 탐지 및 모델 학습.

  * **실시간 스트림 처리 (Kafka Consumer):**

    1.  `sensor-raw-data` 토픽을 구독합니다.
    2.  데이터 수신 시, **Redis**에 실시간 특징을 업데이트합니다.
          * `SET machine:001:last_torque 42.8`
          * (추가 기능) 1분 평균값 등 집계 데이터 저장
    3.  학습된 AI 모델(또는 단순 규칙)과 Redis 데이터를 조합하여 '이상 징후'를 추론합니다.

  * **이상 탐지 시 (Transactional Outbox Pattern):**

    1.  `is_anomaly == True` 일 때, \*\*자신의 로컬 DB (PostgreSQL - AI DB)\*\*에 **단일 트랜잭션**으로 2개 테이블에 `INSERT`합니다.
    2.  `INSERT INTO anomaly_history (machine_id, details...)`
    3.  `INSERT INTO outbox (topic_name, message_payload)`
          * `topic_name`: "anomaly-alerts"
          * `message_payload`: `{"machine_id": "machine-001", "failure_type": "Overstrain", "timestamp": "..."}`

  * **모델 학습 API (Batch):**

      * `POST /train-model`
      * **동작:** (Service-B)의 `sensor_log` 테이블 데이터를 읽어와 (PySpark/Pandas) 모델(Torch)을 학습시키고 `*.pth` 파일로 저장합니다.

### 🖥️ [Service-D] Portal API (Spring Boot)

  * **역할:** 대시보드 API 제공 및 실시간 알람 수신/처리.
  * **Kafka Consumer (알람 수신):**
      * `@KafkaListener(topics = "anomaly-alerts")`
      * Debezium이 발행한 알람 메시지를 구독합니다.
      * **동작:** 수신 시 로그 출력 (e.g., "\!\!긴급\!\! machine-001 과부하 감지"). (추후 이메일, 슬랙 발송 로직으로 확장)
  * **API Endpoints:**
      * `GET /api/machines/{machine_id}/logs`: 특정 설비의 센서 이력 조회 (Ingestor의 `sensor_log` 테이블 조회)
      * `GET /api/machines/{machine_id}/status`: 특정 설비의 현재 상태 조회 (AI 서버의 Redis 데이터 조회)
      * `GET /api/alerts`: 발생한 모든 이상 탐지 알람 내역 조회 (AI 서버의 `anomaly_history` 테이블 조회)

-----

## 5\. 핵심 구현 패턴

### 1\. Transactional Outbox & CDC (AI 서버 → 포털 서버)

  * **목표:** AI 서버가 이상 탐지 후 DB 저장과 Kafka 발행을 **원자적으로** 처리하여 데이터 정합성을 보장합니다. (DB엔 저장됐는데 Kafka 발행은 실패하는 문제 방지)
  * **구현:**
    1.  AI 서버는 이상 탐지 결과를 `outbox` 테이블에만 `INSERT`합니다. (Kafka에 직접 발행 X)
    2.  \*\*Debezium (CDC)\*\*이 `outbox` 테이블의 `INSERT`를 감지(Capture)합니다.
    3.  Debezium이 해당 데이터를 Kafka의 `anomaly-alerts` 토픽으로 대신 발행(Relay)합니다.
    4.  Portal API는 이 `anomaly-alerts` 토픽을 구독하여 알람을 받습니다.

### 2\. 실시간 특징 추출 (Redis 활용)

  * **목표:** 단순 센서 값(`Torque=70`)이 아닌, "최근 1분간 평균 토크가 65 이상" 같은 복합적인 규칙으로 이상 탐지 정확도를 높입니다.
  * **구현:**
    1.  AI 서버(또는 별도 스트림 프로세서)가 `sensor-raw-data`를 구독합니다.
    2.  데이터가 들어올 때마다 Redis에 `machine_id`별로 최신 값/집계 값을 `UPDATE`합니다.
    3.  AI 서버의 추론 로직은 Kafka에서 받은 현재 값 + Redis의 집계 값을 함께 사용하여 판단합니다.