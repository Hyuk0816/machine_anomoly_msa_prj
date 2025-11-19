# Docker 환경 설정 가이드

## 📋 사전 준비

### 1. Docker Network 생성
```bash
docker network create machine_anomaly_msa_network
```

### 2. Network 확인
```bash
docker network ls | grep machine_anomaly_msa_network
```

---

## 🚀 실행 방법

### 전체 서비스 시작
```bash
docker-compose up -d
```

### 개별 서비스 시작
```bash
# PostgreSQL만 시작
docker-compose up -d postgres

# Kafka만 시작 (Zookeeper 자동 시작)
docker-compose up -d kafka

# Kafka UI만 시작
docker-compose up -d kafka-ui
```

### 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f postgres
docker-compose logs -f kafka
docker-compose logs -f kafka-ui
```

---

## 🔍 서비스 접속 정보

### PostgreSQL
- **Host**: localhost
- **Port**: 5432
- **Database**: machine_anomoly
- **User**: admin
- **Password**: admin1234

**연결 문자열**:
```
jdbc:postgresql://localhost:5432/machine_anomoly
```

### Kafka
- **Bootstrap Server**: localhost:9092
- **Topic**: sensor-raw-data (자동 생성)

### Kafka UI (모니터링)
- **URL**: http://localhost:8090
- Kafka 토픽, 메시지, 컨슈머 그룹 모니터링 가능

---

## 🛠️ 유용한 명령어

### 서비스 상태 확인
```bash
docker-compose ps
```

### 헬스체크 확인
```bash
# PostgreSQL
docker exec machine_anomoly_postgres pg_isready -U admin -d machine_anomoly

# Kafka
docker exec machine_anomoly_kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### 서비스 재시작
```bash
docker-compose restart
```

### 서비스 중지
```bash
docker-compose down
```

### 데이터 포함 완전 삭제
```bash
docker-compose down -v
```

---

## 📊 Kafka 토픽 관리

### 토픽 생성 (수동)
```bash
docker exec machine_anomoly_kafka kafka-topics \
  --create \
  --bootstrap-server localhost:9092 \
  --topic sensor-raw-data \
  --partitions 3 \
  --replication-factor 1
```

### 토픽 목록 조회
```bash
docker exec machine_anomoly_kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

### 토픽 상세 정보
```bash
docker exec machine_anomoly_kafka kafka-topics \
  --describe \
  --bootstrap-server localhost:9092 \
  --topic sensor-raw-data
```

### 메시지 컨슈밍 (테스트)
```bash
docker exec machine_anomoly_kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic sensor-raw-data \
  --from-beginning
```

---

## 🗄️ PostgreSQL 접속

### psql 접속
```bash
docker exec -it machine_anomoly_postgres psql -U admin -d machine_anomoly
```

### SQL 쿼리 실행
```bash
# 테이블 목록
docker exec -it machine_anomoly_postgres psql -U admin -d machine_anomoly -c "\dt"

# 데이터 확인
docker exec -it machine_anomoly_postgres psql -U admin -d machine_anomoly -c "SELECT * FROM machine LIMIT 10;"
```

---

## ⚠️ 트러블슈팅

### Network 오류 발생 시
```bash
# Network가 없는 경우
docker network create machine_anomoly_msa_network

# Network 재생성
docker network rm machine_anomoly_msa_network
docker network create machine_anomoly_msa_network
```

### Port 충돌 시
`.env` 파일에서 포트 번호 변경:
```env
POSTGRES_PORT=5433  # 5432 → 5433
KAFKA_PORT=9093     # 9092 → 9093
KAFKA_UI_PORT=8091  # 8090 → 8091
```

### 데이터 초기화
```bash
# 모든 볼륨 삭제하고 재시작
docker-compose down -v
docker-compose up -d
```

---

## 📝 application.yml 연동

Spring Boot `application.yml`에서 다음과 같이 설정:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/machine_anomoly
    username: admin
    password: admin1234

  kafka:
    bootstrap-servers: localhost:9092
```