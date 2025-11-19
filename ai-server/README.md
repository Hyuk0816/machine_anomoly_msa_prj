# AI Anomaly Detection Server

실시간 머신 이상 탐지를 위한 AI 서버

## 개요

이 서버는 Kafka로부터 센서 데이터를 수신하여 XGBoost 모델로 실시간 이상 탐지를 수행하고,
Outbox 패턴과 Debezium CDC를 통해 결과를 Portal로 전달합니다.

## 아키텍처

```
[Data Simulator] → Kafka(sensor-raw-data) → [AI Server Consumer]
                                                ↓
                                           이상 탐지
                                                ↓
                                           Outbox 저장
                                                ↓
                                        Debezium CDC
                                                ↓
                                        Kafka(anomaly-alerts)
                                                ↓
                                            [Portal]
```

## 주요 기능

- ✅ **실시간 이상 탐지**: XGBoost 모델을 사용한 고정밀 예측 (Accuracy 98%)
- ✅ **특징 공학**: 학습 파이프라인과 정확히 동일한 5개 파생 특징 생성
- ✅ **머신 타입 캐싱**: TTLCache + PostgreSQL로 효율적인 타입 조회
- ✅ **Outbox 패턴**: 트랜잭셔널 메시징 보장
- ✅ **FastAPI**: 비동기 REST API 제공
- ✅ **MSA 원칙**: DB 독립성, Kafka 기반 느슨한 결합

## 기술 스택

- **Python 3.10+**
- **FastAPI**: 비동기 웹 프레임워크
- **XGBoost**: ML 모델
- **Kafka**: 이벤트 스트리밍 (kafka-python)
- **PostgreSQL**: Portal DB(타입 조회) + AI Server DB(Outbox)
- **SQLAlchemy 2.0**: ORM
- **Pydantic Settings**: 타입 안전 설정
- **Uvicorn**: ASGI 서버

## 디렉토리 구조

```
ai-server/
├── .env                    # 환경 변수
├── requirements.txt        # 운영 의존성
├── main.py                # FastAPI 메인 애플리케이션
├── run_consumer.py        # Kafka Consumer 실행 스크립트
└── src/
    ├── config.py          # 중앙화된 설정 관리
    ├── preprocessing/     # 특징 공학
    │   └── feature_engineering.py
    ├── ml/               # ML 모델 로딩 및 예측
    │   ├── model_loader.py
    │   └── predictor.py
    ├── cache/            # 머신 타입 캐싱
    │   └── machine_cache.py
    ├── db/               # 데이터베이스 레이어
    │   ├── models.py
    │   └── repositories.py
    ├── kafka/            # Kafka Consumer/Producer
    │   ├── consumer.py
    │   └── producer.py
    └── api/              # FastAPI 라우터
        └── routers.py
```

## 설치 및 실행

### 1. 의존성 설치

```bash
cd ai-server
pip install -r requirements.txt
```

### 2. 환경 변수 설정

`.env` 파일을 확인하고 필요 시 수정:

```bash
# Portal DB (머신 타입 조회용)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=machine_anomaly
DB_USER=admin
DB_PASSWORD=admin1234

# AI Server DB (Outbox 패턴용)
AI_DB_HOST=localhost
AI_DB_PORT=5432
AI_DB_NAME=machine_anomaly_ai_server
AI_DB_USER=admin
AI_DB_PASSWORD=admin1234

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_SENSOR=sensor-raw-data
KAFKA_TOPIC_ALERT=anomaly-alerts
KAFKA_GROUP_ID=ai-server-group

# ML 모델 경로 (ai-server 기준 상대 경로)
MODEL_PATH=../ai-model/models/final_model_xgboost.pkl
SCALER_PATH=../ai-model/models/scaler.pkl
ENCODER_PATH=../ai-model/models/label_encoder_type.pkl
FEATURE_NAMES_PATH=../ai-model/models/feature_names.pkl
```

### 3. AI 모델 학습 (최초 1회)

```bash
cd ../ai-model
jupyter notebook predictive_maintenance_analysis.ipynb
# 노트북에서 모델 학습 실행 → models/ 디렉토리에 pkl 파일 생성
```

### 4. FastAPI 서버 실행

```bash
cd ai-server
python main.py
```

또는 uvicorn 직접 실행:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 5. Kafka Consumer 실행 (별도 터미널)

```bash
python run_consumer.py
```

## API 엔드포인트

### 헬스체크

```bash
GET http://localhost:8000/health
```

**응답 예시**:
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
    "model_type": "XGBClassifier",
    "n_features": 11
  }
}
```

### 수동 예측 (테스트용)

```bash
POST http://localhost:8000/predict
Content-Type: application/json

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

**응답 예시**:
```json
{
  "machine_id": 1,
  "is_anomaly": false,
  "prediction": 0,
  "machine_type": "M",
  "normal_probability": 0.9832,
  "anomaly_probability": 0.0168,
  "features": {
    "Type_encoded": 1,
    "Air_temperature_K": 300.5,
    "Process_temperature_K": 310.2,
    "Rotational_speed_rpm": 1500.0,
    "Torque_Nm": 42.5,
    "Tool_wear_min": 100.0,
    "Temp_diff": 9.7,
    "Power": 63.75,
    "Tool_wear_rate": 0.0666,
    "Torque_speed_ratio": 0.0283,
    "Temp_toolwear": 31020.0
  }
}
```

### 시스템 정보 조회

```bash
GET http://localhost:8000/system/info
```

### API 문서

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## 운영 가이드

### 로그 레벨 설정

`.env`에서 `LOG_LEVEL` 변경:

```bash
LOG_LEVEL=INFO  # DEBUG, INFO, WARNING, ERROR, CRITICAL
```

### 캐시 설정 조정

```bash
CACHE_TTL=3600      # TTL (초)
CACHE_MAXSIZE=1000  # 최대 캐시 항목 수
```

### 데이터베이스 마이그레이션

운영 환경에서는 Alembic 사용:

```bash
# 마이그레이션 초기화
alembic init alembic

# 마이그레이션 파일 생성
alembic revision --autogenerate -m "Create outbox table"

# 마이그레이션 적용
alembic upgrade head
```

## 트러블슈팅

### 모델 파일을 찾을 수 없음

```bash
FileNotFoundError: Model file not found: ...
```

**해결**: ai-model 디렉토리에서 Jupyter notebook을 실행하여 모델 학습

### Kafka 연결 실패

```bash
KafkaError: NoBrokersAvailable
```

**해결**: Kafka 브로커가 실행 중인지 확인

```bash
# Kafka 상태 확인
kafka-broker-api-versions --bootstrap-server localhost:9092
```

### PostgreSQL 연결 실패

```bash
OperationalError: could not connect to server
```

**해결**: PostgreSQL 서비스 상태 및 .env 설정 확인

```bash
# PostgreSQL 상태 확인
pg_isready -h localhost -p 5432
```

## 모니터링

### 캐시 통계 조회

```bash
GET http://localhost:8000/cache/stats
```

### Kafka Consumer 메트릭

Consumer 로그에서 메트릭 확인:
- `records_consumed_rate`: 초당 소비 레코드 수
- `fetch_latency_avg`: 평균 페치 지연시간

## 개발 가이드

### 테스트 실행

```bash
pytest tests/
```

### 코드 포맷팅

```bash
black src/
isort src/
```

### 타입 체크

```bash
mypy src/
```

## 라이선스

MIT