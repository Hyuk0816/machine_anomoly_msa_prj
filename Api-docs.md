# API Documentation

실시간 스마트 팩토리 예지보전 MSA 시스템 API 명세서

## 목차

- [Portal API (Spring Boot)](#portal-api-spring-boot)
  - [Machine API](#machine-api)
  - [DCP Config API](#dcp-config-api)
  - [Anomaly History API](#anomaly-history-api)
  - [Machine Sensor Data API](#machine-sensor-data-api)
  - [SSE API](#sse-api)
- [AI Server API (FastAPI)](#ai-server-api-fastapi)
- [Error Response](#error-response)
- [Data Types](#data-types)

---

## Portal API (Spring Boot)

Base URL: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

### Machine API

설비 관리 API

#### GET /api/machine

모든 설비 목록을 조회합니다.

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |

**Response Body**: `List<MachineResponseDto>`

```json
[
  {
    "id": 1,
    "name": "CNC Machine 1",
    "type": "HIGH"
  },
  {
    "id": 2,
    "name": "CNC Machine 2",
    "type": "MEDIUM"
  }
]
```

---

#### GET /api/machine/{id}

특정 설비의 상세 정보를 조회합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | 설비 ID |

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |
| 404 | 설비를 찾을 수 없음 |

**Response Body**: `MachineResponseDto`

```json
{
  "id": 1,
  "name": "CNC Machine 1",
  "type": "HIGH"
}
```

**Error Response (404)**

```json
{
  "code": "ERROR_MACHINE_NOT_FOUND",
  "message": "설비를 찾을 수 없습니다."
}
```

---

#### POST /api/machine

새로운 설비를 등록합니다.

**Request Body**: `MachineCreateDto`

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| name | String | Yes | 설비 이름 | "CNC Machine 1" |
| type | String | Yes | 설비 유형 (LOW, MEDIUM, HIGH) | "HIGH" |

```json
{
  "name": "CNC Machine 1",
  "type": "HIGH"
}
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 201 | 설비 생성 성공 |
| 400 | 잘못된 요청 데이터 |

---

#### PUT /api/machine/{id}

기존 설비 정보를 수정합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | 설비 ID |

**Request Body**: `MachineModifyDto`

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| name | String | No | 설비 이름 | "Updated CNC Machine 1" |
| type | String | No | 설비 유형 (LOW, MEDIUM, HIGH) | "MEDIUM" |

```json
{
  "name": "Updated CNC Machine 1",
  "type": "MEDIUM"
}
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 수정 성공 |
| 404 | 설비를 찾을 수 없음 |
| 400 | 잘못된 요청 데이터 |

---

#### DELETE /api/machine/{id}

특정 설비를 삭제합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | 설비 ID |

**Response**

| Status Code | Description |
|-------------|-------------|
| 204 | 삭제 성공 |
| 404 | 설비를 찾을 수 없음 |

---

### DCP Config API

데이터 수집 계획(DCP) 설정 관리 API

#### GET /api/dcp-config

모든 DCP 설정을 조회합니다.

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |

**Response Body**: `List<DcpConfigResponseDto>`

```json
[
  {
    "id": 1,
    "machineId": 1,
    "collectInterval": 60,
    "apiEndpoint": "http://simulator:8081/api/sensor/data"
  }
]
```

---

#### GET /api/dcp-config/{id}

특정 DCP 설정의 상세 정보를 조회합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | DCP 설정 ID |

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |
| 404 | DCP 설정을 찾을 수 없음 |

**Response Body**: `DcpConfigResponseDto`

```json
{
  "id": 1,
  "machineId": 1,
  "collectInterval": 60,
  "apiEndpoint": "http://simulator:8081/api/sensor/data"
}
```

**Error Response (404)**

```json
{
  "code": "DCP_NOT_FOUNT",
  "message": "DCP 설정을 찾을 수 없습니다."
}
```

---

#### POST /api/dcp-config

새로운 DCP 설정을 등록하고 스케줄러를 시작합니다.

**Request Body**: `DcpConfigCreateDto`

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| machineId | Long | Yes | 설비 ID | 1 |
| collectInterval | Integer | Yes | 데이터 수집 주기 (초) | 60 |
| apiEndpoint | String | Yes | 데이터 수집 API 엔드포인트 | "http://simulator:8081/api/sensor/data" |

```json
{
  "machineId": 1,
  "collectInterval": 60,
  "apiEndpoint": "http://simulator:8081/api/sensor/data"
}
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 201 | DCP 설정 생성 성공 |
| 400 | 잘못된 요청 데이터 |
| 409 | 해당 설비에 대한 DCP 설정이 이미 존재함 |

**Error Response (409)**

```json
{
  "code": "DCP_MACHINE_DUPLICATED",
  "message": "해당 설비에 대한 DCP 설정이 이미 존재합니다."
}
```

---

#### PUT /api/dcp-config/{id}

기존 DCP 설정을 수정하고 스케줄러를 재시작합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | DCP 설정 ID |

**Request Body**: `DcpConfigModifyDto`

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| machineId | Long | No | 설비 ID | 1 |
| collectInterval | Integer | No | 데이터 수집 주기 (초) | 120 |
| apiEndpoint | String | No | 데이터 수집 API 엔드포인트 | "http://simulator:8081/api/sensor/data" |

```json
{
  "collectInterval": 120,
  "apiEndpoint": "http://simulator:8081/api/sensor/data"
}
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 수정 성공 |
| 404 | DCP 설정을 찾을 수 없음 |
| 400 | 잘못된 요청 데이터 |

---

#### DELETE /api/dcp-config/{id}

DCP 설정을 삭제하고 스케줄러를 중지합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | DCP 설정 ID |

**Response**

| Status Code | Description |
|-------------|-------------|
| 204 | 삭제 성공 |
| 404 | DCP 설정을 찾을 수 없음 |

---

### Anomaly History API

이상 탐지 이력 관리 API

#### GET /api/anomaly-histories

모든 이상 탐지 이력을 조회합니다.

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |

**Response Body**: `List<AnomalyHistoryResponseDto>`

```json
[
  {
    "id": 1,
    "machineId": 1,
    "detectedAt": "2025-11-27T18:30:00",
    "anomalyProbability": 0.85,
    "severity": "CRITICAL",
    "eventMessageSensorData": {
      "airTemperature": 305.5,
      "processTemperature": 315.2,
      "rotationalSpeed": 1800,
      "torque": 55.3,
      "toolWear": 180
    }
  }
]
```

---

#### GET /api/anomaly-histories/search

기간별 이상 탐지 이력을 조회합니다.

**Query Parameters**

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| start | LocalDateTime | Yes | 검색 시작 시간 (ISO-8601) | 2025-01-01T00:00:00 |
| end | LocalDateTime | Yes | 검색 종료 시간 (ISO-8601) | 2025-12-31T23:59:59 |

**Request Example**

```
GET /api/anomaly-histories/search?start=2025-01-01T00:00:00&end=2025-12-31T23:59:59
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |
| 400 | 잘못된 요청 파라미터 |

**Response Body**: `List<AnomalyHistoryResponseDto>`

```json
[
  {
    "id": 1,
    "machineId": 1,
    "detectedAt": "2025-11-27T18:30:00",
    "anomalyProbability": 0.85,
    "severity": "CRITICAL",
    "eventMessageSensorData": {
      "airTemperature": 305.5,
      "processTemperature": 315.2,
      "rotationalSpeed": 1800,
      "torque": 55.3,
      "toolWear": 180
    }
  }
]
```

---

### Machine Sensor Data API

기계 센서 데이터 조회 API

#### GET /api/machine-sensor-data/{machineId}

특정 기계의 지정된 기간 내 센서 데이터를 조회합니다.

**Path Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| machineId | Long | Yes | 기계 ID |

**Query Parameters**

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| startDateTime | LocalDateTime | Yes | 검색 시작 시간 (ISO-8601) | 2025-01-01T00:00:00 |
| endDateTime | LocalDateTime | Yes | 검색 종료 시간 (ISO-8601) | 2025-12-31T23:59:59 |

**Request Example**

```
GET /api/machine-sensor-data/1?startDateTime=2025-01-01T00:00:00&endDateTime=2025-12-31T23:59:59
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |
| 400 | 잘못된 요청 파라미터 |
| 404 | 기계를 찾을 수 없음 |

**Response Body**: `List<MachineSensorDataResponseDto>`

```json
[
  {
    "id": 1,
    "machineId": 1,
    "airTemperature": 300.5,
    "processTemperature": 310.2,
    "rotationalSpeed": 1500,
    "torque": 42.5,
    "toolWear": 100,
    "createdAt": "2025-11-27T18:30:00"
  }
]
```

---

### SSE API

Server-Sent Events 실시간 알림 API

#### GET /api/sse/subscribe

SSE 연결을 구독합니다. 이상 탐지 시 실시간으로 알림을 받습니다.

**Query Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| clientId | String | No | 클라이언트 식별자 (미제공 시 UUID 자동 생성) |

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | SSE 스트림 연결 성공 |

**Response Content-Type**: `text/event-stream`

**SSE Event Name**: `anomaly-alert`

**SSE Event Data**: `AnomalySseDto`

```json
{
  "machineId": 1,
  "machineName": "CNC Machine 1",
  "detectedAt": "2025-11-27T18:30:00",
  "severity": "CRITICAL",
  "anomalyProbability": 0.85
}
```

**Frontend 사용 예시**

```javascript
const eventSource = new EventSource('/api/sse/subscribe');

eventSource.addEventListener('anomaly-alert', (event) => {
  const alert = JSON.parse(event.data);
  console.log('이상 탐지:', alert);
});

eventSource.onerror = () => {
  console.error('SSE 연결 오류');
  // 5초 후 재연결 로직
};
```

---

## AI Server API (FastAPI)

Base URL: `http://localhost:8000`

Swagger UI: `http://localhost:8000/docs`

ReDoc: `http://localhost:8000/redoc`

---

### GET /

루트 엔드포인트 - 서비스 정보를 반환합니다.

**Response**

```json
{
  "service": "AI Anomaly Detection Server",
  "version": "1.0.0",
  "status": "running",
  "endpoints": {
    "health": "/health",
    "predict": "/predict",
    "system_info": "/system/info"
  }
}
```

---

### GET /health

헬스체크 엔드포인트 - 서비스 상태 및 준비 여부를 확인합니다.

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 서비스 정상 |
| 503 | 서비스 비정상 |

**Response Body**: `HealthResponse`

```json
{
  "status": "healthy",
  "version": "1.0.0",
  "predictor_ready": true,
  "cache_info": {
    "current_size": 5,
    "max_size": 1000,
    "ttl_seconds": 3600
  },
  "model_info": {
    "model_type": "XGBoost",
    "feature_count": 10,
    "loaded_at": "2025-11-27T18:00:00"
  }
}
```

---

### POST /predict

이상 탐지 예측 엔드포인트 (수동 테스트용)

실제 운영 환경에서는 Kafka Consumer가 자동으로 예측을 수행하지만, 이 엔드포인트는 수동 테스트 및 디버깅 용도로 사용됩니다.

**Request Body**: `PredictRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| machine_id | int | Yes | 머신 ID |
| sensor_data | object | Yes | 센서 데이터 |

**sensor_data 필드**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| airTemperature | float | 대기 온도 (K) | 300.5 |
| processTemperature | float | 공정 온도 (K) | 310.2 |
| rotationalSpeed | float | 회전 속도 (rpm) | 1500.0 |
| torque | float | 토크 (Nm) | 42.5 |
| toolWear | float | 공구 마모 (min) | 100.0 |

**Request Example**

```json
{
  "machine_id": 1,
  "sensor_data": {
    "airTemperature": 300.5,
    "processTemperature": 310.2,
    "rotationalSpeed": 1500.0,
    "torque": 42.5,
    "toolWear": 100.0
  }
}
```

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 예측 성공 |
| 404 | 머신 ID를 찾을 수 없음 |
| 500 | 예측 실패 |

**Response Body**: `PredictResponse`

```json
{
  "machine_id": 1,
  "is_anomaly": true,
  "prediction": 1,
  "machine_type": "HIGH",
  "normal_probability": 0.15,
  "anomaly_probability": 0.85,
  "features": {
    "airTemperature": 300.5,
    "processTemperature": 310.2,
    "rotationalSpeed": 1500.0,
    "torque": 42.5,
    "toolWear": 100.0,
    "Temp_diff": 9.7,
    "Power": 63750.0,
    "Tool_wear_rate": 0.067,
    "Torque_speed_ratio": 0.028,
    "Temp_toolwear": 31020.0
  }
}
```

**Error Response (404)**

```json
{
  "detail": "Machine ID 999 not found in database"
}
```

---

### GET /system/info

시스템 정보 조회 엔드포인트 - AI Server의 설정 및 상태 정보를 조회합니다.

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |
| 500 | 조회 실패 |

**Response Body**: `SystemInfoResponse`

```json
{
  "config": {
    "api_host": "0.0.0.0",
    "api_port": 8000,
    "log_level": "INFO",
    "cache_ttl": 3600,
    "cache_maxsize": 1000
  },
  "cache_info": {
    "current_size": 5,
    "max_size": 1000,
    "ttl_seconds": 3600
  },
  "model_info": {
    "model_type": "XGBoost",
    "feature_count": 10,
    "loaded_at": "2025-11-27T18:00:00"
  },
  "kafka_config": {
    "bootstrap_servers": "kafka:29092",
    "sensor_topic": "sensor-raw-data",
    "alert_topic": "anomaly-alerts",
    "group_id": "ai-anomaly-detector"
  }
}
```

---

### GET /cache/stats

캐시 통계 조회 엔드포인트 - 머신 타입 캐시의 현재 상태를 조회합니다.

**Response**

| Status Code | Description |
|-------------|-------------|
| 200 | 조회 성공 |
| 500 | 조회 실패 |

**Response Body**

```json
{
  "current_size": 5,
  "max_size": 1000,
  "ttl_seconds": 3600,
  "hit_count": 150,
  "miss_count": 5
}
```

---

## Error Response

### Portal API 에러 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

### 에러 코드 목록

#### Machine Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| ERROR_MACHINE_NOT_FOUND | 404 | 설비를 찾을 수 없습니다 |

**응답 예시:**
```json
{
  "code": "ERROR_MACHINE_NOT_FOUND",
  "message": "설비를 찾을 수 없습니다"
}
```

#### DCP Config Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| DCP_NOT_FOUNT | 404 | DCP 설정을 찾을 수 없습니다 |
| DCP_MACHINE_DUPLICATED | 409 | 해당 설비에 대한 DCP 설정이 이미 존재합니다 |
| DCP_DUPLICATED | 409 | 중복된 DCP 설정이 존재합니다 |

**응답 예시:**
```json
{
  "code": "DCP_NOT_FOUNT",
  "message": "DCP 설정을 찾을 수 없습니다"
}
```

```json
{
  "code": "DCP_MACHINE_DUPLICATED",
  "message": "해당 설비에 대한 DCP 설정이 이미 존재합니다"
}
```

#### Sensor Data Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| ERROR_JSON_WRITE_ERRE | 422 | JSON 변환에 실패했습니다 |

**응답 예시:**
```json
{
  "code": "ERROR_JSON_WRITE_ERRE",
  "message": "JSON 변환에 실패했습니다"
}
```

#### Global Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| ERROR_INTERNAL_SERVER | 500 | 내부 서버 오류가 발생했습니다 |

**응답 예시:**
```json
{
  "code": "ERROR_INTERNAL_SERVER",
  "message": "내부 서버 오류가 발생했습니다"
}
```

### AI Server API 에러 응답 형식

```json
{
  "detail": "에러 메시지"
}
```

**응답 예시:**
```json
{
  "detail": "Machine type not found for machine_id: 1"
}
```

---

## Data Types

### Enums

#### Machine Type

설비 유형을 나타내는 열거형

| Value | Description |
|-------|-------------|
| LOW | 저부하 설비 |
| MEDIUM | 중부하 설비 |
| HIGH | 고부하 설비 |

#### Severity

이상 탐지 심각도를 나타내는 열거형

| Value | Threshold | Description |
|-------|-----------|-------------|
| WARNING | >= 30% | 경고 수준 |
| ALERT | >= 50% | 주의 수준 |
| CRITICAL | >= 70% | 위험 수준 |

### DTOs

#### MachineResponseDto

| Field | Type | Description |
|-------|------|-------------|
| id | Long | 설비 ID |
| name | String | 설비 이름 |
| type | String | 설비 유형 (LOW, MEDIUM, HIGH) |

#### DcpConfigResponseDto

| Field | Type | Description |
|-------|------|-------------|
| id | Long | DCP 설정 ID |
| machineId | Long | 설비 ID |
| collectInterval | Integer | 데이터 수집 주기 (초) |
| apiEndpoint | String | 데이터 수집 API 엔드포인트 |

#### AnomalyHistoryResponseDto

| Field | Type | Description |
|-------|------|-------------|
| id | Long | 이상 이력 ID |
| machineId | Long | 설비 ID |
| detectedAt | String | 탐지 시간 (ISO-8601) |
| anomalyProbability | Double | 이상 확률 (0.0 ~ 1.0) |
| severity | Severity | 심각도 (WARNING, ALERT, CRITICAL) |
| eventMessageSensorData | EventMessageSensorData | 센서 데이터 |

#### EventMessageSensorData

| Field | Type | Description |
|-------|------|-------------|
| airTemperature | Double | 대기 온도 (K) |
| processTemperature | Double | 공정 온도 (K) |
| rotationalSpeed | Integer | 회전 속도 (rpm) |
| torque | Double | 토크 (Nm) |
| toolWear | Integer | 공구 마모 (min) |

#### MachineSensorDataResponseDto

| Field | Type | Description |
|-------|------|-------------|
| id | Long | 센서 데이터 ID |
| machineId | Long | 설비 ID |
| airTemperature | Double | 대기 온도 (K) |
| processTemperature | Double | 공정 온도 (K) |
| rotationalSpeed | Integer | 회전 속도 (rpm) |
| torque | Double | 토크 (Nm) |
| toolWear | Integer | 공구 마모 (min) |
| createdAt | LocalDateTime | 생성 시간 |

#### AnomalySseDto

| Field | Type | Description |
|-------|------|-------------|
| machineId | Long | 설비 ID |
| machineName | String | 설비 이름 |
| detectedAt | LocalDateTime | 탐지 시간 |
| severity | String | 심각도 (WARNING, ALERT, CRITICAL) |
| anomalyProbability | Double | 이상 확률 (0.0 ~ 1.0) |